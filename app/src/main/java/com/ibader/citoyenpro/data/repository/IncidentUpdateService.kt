package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity
import com.ibader.citoyenpro.domain.model.CitizenPointsRules
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.notification.IncidentStatusNotifier

// Point d'entrée unique pour modifier un signalement côté admin (statut ou
// service affecté) : persiste le changement, journalise l'historique,
// notifie le citoyen concerné et (pour un changement de statut) le crédite
// en points. Centraliser ces effets ici évite qu'un futur appelant (autre
// écran admin, puis synchronisation backend) n'en oublie un.
class IncidentUpdateService(
    private val incidentRepository: IncidentRepository,
    private val incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    private val incidentStatusNotifier: IncidentStatusNotifier,
    private val userRepository: UserRepository
) {
    suspend fun updateStatus(incidentId: Long, newStatus: IncidentStatus, commentaire: String? = null) {
        val updated = incidentRepository.updateStatus(incidentId, newStatus) ?: return

        incidentStatusHistoryRepository.insert(
            IncidentStatusHistoryEntity(
                incidentId = incidentId,
                status = newStatus,
                date = updated.dateMaj,
                commentaire = commentaire
            )
        )

        userRepository.addPoints(updated.citoyenId, CitizenPointsRules.CHANGEMENT_STATUT)
        incidentStatusNotifier.notifyStatusChanged(updated, newStatus)
    }

    // Le statut ne change pas lors d'une affectation de service : l'entrée
    // d'historique journalise donc le statut courant, avec un commentaire
    // décrivant l'affectation, pour garder une trace unique et chronologique
    // de toutes les actions admin sur le signalement.
    suspend fun assignService(incidentId: Long, service: String?) {
        val updated = incidentRepository.updateServiceAffecte(incidentId, service) ?: return

        incidentStatusHistoryRepository.insert(
            IncidentStatusHistoryEntity(
                incidentId = incidentId,
                status = updated.status,
                date = updated.dateMaj,
                commentaire = if (service.isNullOrBlank()) {
                    "Service affecté retiré"
                } else {
                    "Service affecté : $service"
                }
            )
        )

        incidentStatusNotifier.notifyServiceAssigned(updated, service)
    }
}
