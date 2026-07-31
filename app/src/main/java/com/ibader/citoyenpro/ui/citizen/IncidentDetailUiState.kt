package com.ibader.citoyenpro.ui.citizen

import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity

data class IncidentDetailUiState(
    val isLoading: Boolean = true,
    val incident: IncidentEntity? = null,
    val categoryNom: String = "",
    val history: List<IncidentStatusHistoryEntity> = emptyList()
)
