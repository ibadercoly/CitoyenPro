package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.IncidentDao
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.domain.model.IncidentStatus
import kotlinx.coroutines.flow.Flow

// S'appuie uniquement sur Room pour l'instant ; une source réseau (Retrofit)
// sera introduite plus tard pour synchroniser les signalements avec le backend.
class IncidentRepository(
    private val incidentDao: IncidentDao
) {
    suspend fun insert(incident: IncidentEntity): Long = incidentDao.insert(incident)

    suspend fun update(incident: IncidentEntity) = incidentDao.update(incident)

    suspend fun delete(incident: IncidentEntity) = incidentDao.delete(incident)

    suspend fun getById(id: Long): IncidentEntity? = incidentDao.getById(id)

    fun getAll(): Flow<List<IncidentEntity>> = incidentDao.getAll()

    fun getByCitoyen(citoyenId: Long): Flow<List<IncidentEntity>> = incidentDao.getByCitoyen(citoyenId)

    fun getByStatus(status: IncidentStatus): Flow<List<IncidentEntity>> = incidentDao.getByStatus(status)

    fun getByCategory(categoryId: Long): Flow<List<IncidentEntity>> = incidentDao.getByCategory(categoryId)
}
