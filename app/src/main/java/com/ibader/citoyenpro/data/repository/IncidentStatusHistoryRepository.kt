package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.IncidentStatusHistoryDao
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity
import kotlinx.coroutines.flow.Flow

// S'appuie uniquement sur Room pour l'instant ; une source réseau (Retrofit)
// sera introduite plus tard pour synchroniser l'historique avec le backend.
class IncidentStatusHistoryRepository(
    private val incidentStatusHistoryDao: IncidentStatusHistoryDao
) {
    suspend fun insert(entry: IncidentStatusHistoryEntity): Long = incidentStatusHistoryDao.insert(entry)

    fun getByIncident(incidentId: Long): Flow<List<IncidentStatusHistoryEntity>> =
        incidentStatusHistoryDao.getByIncident(incidentId)
}
