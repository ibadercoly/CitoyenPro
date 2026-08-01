package com.ibader.citoyenpro.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Un citoyen ne peut voter qu'une fois par incident : contrainte imposée par
// l'index unique (incidentId, citoyenId), pas seulement côté UI.
@Entity(
    tableName = "incident_votes",
    foreignKeys = [
        ForeignKey(
            entity = IncidentEntity::class,
            parentColumns = ["id"],
            childColumns = ["incidentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["citoyenId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["incidentId", "citoyenId"], unique = true),
        Index(value = ["citoyenId"])
    ]
)
data class IncidentVoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val incidentId: Long,
    val citoyenId: Long,
    val dateVote: Long
)