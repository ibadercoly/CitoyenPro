package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.IncidentVoteCount
import com.ibader.citoyenpro.data.local.dao.IncidentVoteDao
import com.ibader.citoyenpro.data.local.entities.IncidentVoteEntity
import kotlinx.coroutines.flow.Flow

// S'appuie uniquement sur Room ; contrairement à IncidentRepository, les
// votes ne sont pas poussés vers le backend pour l'instant (pas d'endpoint
// dédié) — comme l'historique des statuts, une donnée locale dérivée plutôt
// que synchronisée.
class IncidentVoteRepository(
    private val incidentVoteDao: IncidentVoteDao
) {
    // Result plutôt qu'une exception qui remonterait jusqu'à l'appelant :
    // l'index unique (incidentId, citoyenId) fait déjà respecter la règle
    // "un vote par citoyen et par incident" au niveau base de données, même
    // si l'UI désactive déjà le bouton après un premier vote.
    suspend fun vote(incidentId: Long, citoyenId: Long): Result<Unit> = runCatching {
        incidentVoteDao.insert(
            IncidentVoteEntity(incidentId = incidentId, citoyenId = citoyenId, dateVote = System.currentTimeMillis())
        )
        Unit
    }

    fun hasVoted(incidentId: Long, citoyenId: Long): Flow<Boolean> = incidentVoteDao.hasVoted(incidentId, citoyenId)

    fun getVoteCounts(): Flow<List<IncidentVoteCount>> = incidentVoteDao.getVoteCounts()

    fun getVotedIncidentIds(citoyenId: Long): Flow<List<Long>> = incidentVoteDao.getVotedIncidentIds(citoyenId)
}