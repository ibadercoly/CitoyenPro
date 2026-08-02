package com.ibader.citoyenpro.ui.admin

import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity

data class AdminIncidentDetailUiState(
    val isLoading: Boolean = true,
    val incident: IncidentEntity? = null,
    val categoryNom: String = "",
    val history: List<IncidentStatusHistoryEntity> = emptyList(),
    val showDeleteConfirmation: Boolean = false,
    val isDeleted: Boolean = false
)
