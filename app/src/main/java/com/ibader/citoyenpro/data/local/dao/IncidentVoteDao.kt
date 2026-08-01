package com.ibader.citoyenpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ibader.citoyenpro.data.local.entities.IncidentVoteEntity
import kotlinx.coroutines.flow.Flow

data class IncidentVoteCount(val incidentId: Long, val count: Int)

@Dao
interface IncidentVoteDao {

    @Insert
    suspend fun insert(vote: IncidentVoteEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM incident_votes WHERE incidentId = :incidentId AND citoyenId = :citoyenId)")
    fun hasVoted(incidentId: Long, citoyenId: Long): Flow<Boolean>

    // Un par incident voté, pas par incident existant : les incidents sans
    // aucun vote sont simplement absents de cette liste (à traiter comme 0
    // côté appelant plutôt que via une jointure gauche).
    @Query("SELECT incidentId, COUNT(*) as count FROM incident_votes GROUP BY incidentId")
    fun getVoteCounts(): Flow<List<IncidentVoteCount>>

    @Query("SELECT incidentId FROM incident_votes WHERE citoyenId = :citoyenId")
    fun getVotedIncidentIds(citoyenId: Long): Flow<List<Long>>
}