package com.ibader.citoyenpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentStatusHistoryDao {

    @Insert
    suspend fun insert(entry: IncidentStatusHistoryEntity): Long

    @Query("SELECT * FROM incident_status_history WHERE incidentId = :incidentId ORDER BY date ASC")
    fun getByIncident(incidentId: Long): Flow<List<IncidentStatusHistoryEntity>>

    // Utilisé par IncidentRepository pour faire pointer l'historique vers
    // l'id serveur d'un incident après sa création (cf. reconcileLocalId).
    @Query("UPDATE incident_status_history SET incidentId = :newIncidentId WHERE incidentId = :oldIncidentId")
    suspend fun reassignIncidentId(oldIncidentId: Long, newIncidentId: Long)
}
