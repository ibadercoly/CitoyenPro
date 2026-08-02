package com.ibader.citoyenpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.domain.model.IncidentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    @Insert
    suspend fun insert(incident: IncidentEntity): Long

    @Update
    suspend fun update(incident: IncidentEntity)

    // Insertion ou remplacement par id : utilisé pour fusionner les
    // signalements reçus de l'API dans Room (IncidentRepository.pullRemoteIncidents),
    // où l'id distant doit être pris tel quel plutôt que ré-autogénéré.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(incident: IncidentEntity)

    @Delete
    suspend fun delete(incident: IncidentEntity)

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getById(id: Long): IncidentEntity?

    @Query("SELECT * FROM incidents WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents ORDER BY date_creation DESC")
    fun getAll(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE citoyenUid = :citoyenUid ORDER BY date_creation DESC")
    fun getByCitoyen(citoyenUid: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE status = :status ORDER BY date_creation DESC")
    fun getByStatus(status: IncidentStatus): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE categoryId = :categoryId ORDER BY date_creation DESC")
    fun getByCategory(categoryId: Long): Flow<List<IncidentEntity>>
}
