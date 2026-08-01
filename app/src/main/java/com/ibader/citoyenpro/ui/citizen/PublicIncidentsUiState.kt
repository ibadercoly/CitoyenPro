package com.ibader.citoyenpro.ui.citizen

import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority

data class PublicIncidentListItem(
    val id: Long,
    val titre: String,
    val categoryNom: String,
    val status: IncidentStatus,
    val priority: Priority,
    val dateCreation: Long,
    val voteCount: Int,
    val hasVoted: Boolean
)

data class PublicIncidentsUiState(
    val isLoading: Boolean = true,
    val items: List<PublicIncidentListItem> = emptyList()
)