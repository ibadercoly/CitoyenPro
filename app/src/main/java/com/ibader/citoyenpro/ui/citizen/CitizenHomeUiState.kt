package com.ibader.citoyenpro.ui.citizen

import com.ibader.citoyenpro.domain.model.IncidentStatus

data class CitizenHomeStatusCount(val status: IncidentStatus, val count: Int)

data class CitizenHomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val totalIncidents: Int = 0,
    val byStatus: List<CitizenHomeStatusCount> = emptyList(),
    val recentIncidents: List<IncidentListItem> = emptyList()
)
