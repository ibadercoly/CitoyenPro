package com.ibader.citoyenpro.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PendingOperationType { CREATE, UPDATE, DELETE }

// File d'attente des écritures sur un signalement à rejouer vers l'API
// quand le réseau était indisponible au moment où elles ont été faites en
// local (cf. IncidentRepository). Un seul enregistrement par incident : les
// écritures successives sur le même incident se fusionnent plutôt que de
// s'empiler (voir IncidentRepository.resolveOperationType).
@Entity(
    tableName = "pending_incident_operations",
    indices = [Index(value = ["incidentId"], unique = true)]
)
data class PendingIncidentOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val incidentId: Long,
    val operationType: PendingOperationType,
    val createdAt: Long
)
