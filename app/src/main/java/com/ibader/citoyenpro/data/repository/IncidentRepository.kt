package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.IncidentDao
import com.ibader.citoyenpro.data.local.dao.PendingIncidentOperationDao
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.PendingIncidentOperationEntity
import com.ibader.citoyenpro.data.local.entities.PendingOperationType
import com.ibader.citoyenpro.data.remote.ApiService
import com.ibader.citoyenpro.data.remote.mapper.toDto
import com.ibader.citoyenpro.data.remote.mapper.toEntity
import com.ibader.citoyenpro.data.sync.SyncStatusHolder
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow

// Room est la source unique de vérité affichée à l'UI (toutes les lectures
// ci-dessous viennent d'elle, jamais directement de l'API) ; ce repository
// synchronise ce cache local avec le backend :
//
// - Chaque écriture (insert/update/delete) est d'abord appliquée en local —
//   l'UI, qui observe Room, réagit donc immédiatement même hors-ligne — puis
//   poussée vers l'API au mieux. En cas d'échec (pas de réseau, backend
//   injoignable), l'opération est mise en file dans `pending_incident_operations`
//   pour être rejouée par syncPendingChanges().
// - syncPendingChanges() rejoue la file dans l'ordre, puis complète Room avec
//   les signalements distants (créés/modifiés par un autre client) — sans
//   jamais supprimer localement ce que l'API ne renvoie pas : une suppression
//   ne peut venir que d'un delete() explicite, passé par la même file.
//
// syncPendingChanges() est aussi appelé automatiquement : au lancement de
// l'app (AppNavHost), dès que la connectivité revient (ConnectivitySyncTrigger
// + IncidentSyncWorker) et périodiquement en filet de sécurité (WorkManager,
// cf. IncidentSyncWorker.enqueuePeriodic). SyncStatusHolder reflète l'état de
// chaque tentative pour l'indicateur affiché dans l'UI.
class IncidentRepository(
    private val incidentDao: IncidentDao,
    private val pendingOperationDao: PendingIncidentOperationDao,
    private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor
) {

    suspend fun insert(incident: IncidentEntity): Long {
        val id = incidentDao.insert(incident)
        enqueueOrPush(id, PendingOperationType.CREATE)
        return id
    }

    suspend fun update(incident: IncidentEntity) {
        incidentDao.update(incident)
        enqueueOrPush(incident.id, PendingOperationType.UPDATE)
    }

    // Retourne l'incident mis à jour (avec son nouveau statut et sa nouvelle
    // date de mise à jour), ou null si l'incident n'existe plus.
    suspend fun updateStatus(id: Long, status: IncidentStatus): IncidentEntity? {
        val incident = incidentDao.getById(id) ?: return null
        val updated = incident.copy(status = status, dateMaj = System.currentTimeMillis())
        incidentDao.update(updated)
        enqueueOrPush(id, PendingOperationType.UPDATE)
        return updated
    }

    // Retourne l'incident mis à jour, ou null si l'incident n'existe plus.
    suspend fun updateServiceAffecte(id: Long, serviceAffecte: String?): IncidentEntity? {
        val incident = incidentDao.getById(id) ?: return null
        val updated = incident.copy(serviceAffecte = serviceAffecte, dateMaj = System.currentTimeMillis())
        incidentDao.update(updated)
        enqueueOrPush(id, PendingOperationType.UPDATE)
        return updated
    }

    suspend fun delete(incident: IncidentEntity) {
        incidentDao.delete(incident)
        enqueueOrPush(incident.id, PendingOperationType.DELETE)
    }

    suspend fun getById(id: Long): IncidentEntity? = incidentDao.getById(id)

    fun observeById(id: Long): Flow<IncidentEntity?> = incidentDao.getByIdFlow(id)

    fun getAll(): Flow<List<IncidentEntity>> = incidentDao.getAll()

    fun getByCitoyen(citoyenId: Long): Flow<List<IncidentEntity>> = incidentDao.getByCitoyen(citoyenId)

    fun getByStatus(status: IncidentStatus): Flow<List<IncidentEntity>> = incidentDao.getByStatus(status)

    fun getByCategory(categoryId: Long): Flow<List<IncidentEntity>> = incidentDao.getByCategory(categoryId)

    // Nombre d'opérations en attente d'envoi, pour l'indicateur de synchro de l'UI.
    val pendingOperationCount: Flow<Int> = pendingOperationDao.observeCount()

    // Rejoue la file d'attente puis tire les dernières données distantes ;
    // reporte chaque étape dans SyncStatusHolder pour l'indicateur de l'UI.
    // Ne fait rien (sync à part) si le réseau semble indisponible (évite des
    // appels voués à l'échec) ; sans réseau ou si l'API reste injoignable, la
    // file est simplement conservée pour un prochain appel. Retourne true si
    // la file a été entièrement rejouée et les données distantes récupérées
    // (utilisé par IncidentSyncWorker pour décider d'un retry WorkManager).
    suspend fun syncPendingChanges(): Boolean {
        if (!networkMonitor.isOnline()) {
            SyncStatusHolder.onOffline()
            return false
        }
        SyncStatusHolder.onSyncStarted()
        val pushedAll = pushPendingOperations()
        val pulled = pullRemoteIncidents()
        val success = pushedAll && pulled
        if (success) SyncStatusHolder.onSyncSucceeded() else SyncStatusHolder.onSyncFailed()
        return success
    }

    // S'arrête à la première opération qui échoue encore, pour conserver
    // l'ordre de la file plutôt que de rejouer les suivantes hors ordre.
    // Retourne true seulement si la file a été intégralement vidée.
    private suspend fun pushPendingOperations(): Boolean {
        for (operation in pendingOperationDao.getAllOrderedByDate()) {
            if (pushToApi(operation)) {
                pendingOperationDao.delete(operation.id)
            } else {
                return false
            }
        }
        return true
    }

    // N'écrase jamais un incident qui a encore une opération en file : son
    // état local est intentionnellement en avance sur le serveur tant que
    // cette opération n'a pas été poussée avec succès. Ne supprime jamais un
    // incident absent de la réponse : seul delete() (via la file) le peut.
    private suspend fun pullRemoteIncidents(): Boolean {
        val remoteIncidents = runCatching { apiService.getIncidents() }.getOrNull() ?: return false
        val pendingIds = pendingOperationDao.getAllOrderedByDate().map { it.incidentId }.toSet()

        remoteIncidents.forEach { dto ->
            val id = dto.id ?: return@forEach
            if (id in pendingIds) return@forEach
            incidentDao.upsert(dto.toEntity())
        }
        return true
    }

    // Fusionne l'opération en file existante pour cet incident (s'il y en a
    // une) avec la nouvelle, puis tente de la pousser immédiatement si le
    // réseau semble disponible ; sinon (ou en cas d'échec) la (re)met en file.
    private suspend fun enqueueOrPush(incidentId: Long, type: PendingOperationType) {
        val existing = pendingOperationDao.getForIncident(incidentId)
        val resolvedType = resolveOperationType(existing?.operationType, type)

        if (resolvedType == null) {
            // CREATE annulé par un DELETE avant toute synchronisation : rien
            // n'a jamais existé côté serveur, aucun appel réseau n'est nécessaire.
            existing?.let { pendingOperationDao.delete(it.id) }
            return
        }

        pendingOperationDao.deleteForIncident(incidentId)
        val pending = PendingIncidentOperationEntity(
            incidentId = incidentId,
            operationType = resolvedType,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        if (!networkMonitor.isOnline() || !pushToApi(pending)) {
            pendingOperationDao.insert(pending)
        }
    }

    // null = les deux opérations s'annulent (créé puis supprimé avant toute
    // synchronisation). CREATE absorbe un UPDATE qui suit (la création lira
    // de toute façon l'état local le plus récent au moment du push) mais
    // cède la place à un DELETE qui suivrait un UPDATE, puisque le serveur a
    // alors vraisemblablement déjà la ressource.
    private fun resolveOperationType(
        existing: PendingOperationType?,
        incoming: PendingOperationType
    ): PendingOperationType? = when (existing) {
        null -> incoming
        PendingOperationType.CREATE -> if (incoming == PendingOperationType.DELETE) null else PendingOperationType.CREATE
        PendingOperationType.UPDATE -> if (incoming == PendingOperationType.DELETE) {
            PendingOperationType.DELETE
        } else {
            PendingOperationType.UPDATE
        }
        PendingOperationType.DELETE -> PendingOperationType.DELETE
    }

    // Best-effort : toute erreur (réseau, backend indisponible, réponse en
    // échec) est traitée comme "à retenter plus tard" plutôt que distinguée
    // finement, faute de backend réel contre lequel calibrer ce
    // comportement pour l'instant.
    private suspend fun pushToApi(operation: PendingIncidentOperationEntity): Boolean = runCatching {
        when (operation.operationType) {
            PendingOperationType.CREATE, PendingOperationType.UPDATE -> {
                val incident = incidentDao.getById(operation.incidentId) ?: return@runCatching true
                if (operation.operationType == PendingOperationType.CREATE) {
                    apiService.createIncident(incident.toDto())
                } else {
                    apiService.updateIncident(operation.incidentId, incident.toDto())
                }
            }
            PendingOperationType.DELETE -> apiService.deleteIncident(operation.incidentId)
        }
    }.isSuccess
}
