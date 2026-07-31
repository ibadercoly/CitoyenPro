package com.ibader.citoyenpro.ui.citizen

import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority

data class IncidentListItem(
    val id: Long,
    val titre: String,
    val categoryNom: String,
    val status: IncidentStatus,
    val priority: Priority,
    val dateCreation: Long
)

data class MyIncidentsUiState(
    val isLoading: Boolean = true,
    val items: List<IncidentListItem> = emptyList()
)
