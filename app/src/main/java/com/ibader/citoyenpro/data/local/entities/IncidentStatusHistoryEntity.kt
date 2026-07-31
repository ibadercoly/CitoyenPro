package com.ibader.citoyenpro.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ibader.citoyenpro.domain.model.IncidentStatus

@Entity(
    tableName = "incident_status_history",
    foreignKeys = [
        ForeignKey(
            entity = IncidentEntity::class,
            parentColumns = ["id"],
            childColumns = ["incidentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["incidentId"])]
)
data class IncidentStatusHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val incidentId: Long,
    val status: IncidentStatus,
    val date: Long,
    val commentaire: String? = null
)
