package com.ibader.citoyenpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ibader.citoyenpro.data.local.entities.PendingIncidentOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingIncidentOperationDao {

    @Insert
    suspend fun insert(operation: PendingIncidentOperationEntity): Long

    @Query("DELETE FROM pending_incident_operations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM pending_incident_operations WHERE incidentId = :incidentId")
    suspend fun deleteForIncident(incidentId: Long)

    @Query("SELECT * FROM pending_incident_operations WHERE incidentId = :incidentId LIMIT 1")
    suspend fun getForIncident(incidentId: Long): PendingIncidentOperationEntity?

    @Query("SELECT * FROM pending_incident_operations ORDER BY createdAt")
    suspend fun getAllOrderedByDate(): List<PendingIncidentOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_incident_operations")
    fun observeCount(): Flow<Int>
}
