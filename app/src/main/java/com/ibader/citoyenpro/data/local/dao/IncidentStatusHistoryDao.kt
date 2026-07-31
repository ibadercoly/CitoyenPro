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
}
