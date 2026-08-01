package com.ibader.citoyenpro.ui.admin

import com.ibader.citoyenpro.domain.model.IncidentStatus

data class AdminDashboardStatusCount(val status: IncidentStatus, val count: Int)

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val totalIncidents: Int = 0,
    val byStatus: List<AdminDashboardStatusCount> = emptyList(),
    val recentIncidents: List<AdminIncidentListItem> = emptyList()
)
