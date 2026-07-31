package com.ibader.citoyenpro.ui.admin

import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority

enum class IncidentDateSortOrder {
    RECENT_DABORD,
    ANCIEN_DABORD
}

data class AdminIncidentListItem(
    val id: Long,
    val titre: String,
    val categoryNom: String,
    val status: IncidentStatus,
    val priority: Priority,
    val dateCreation: Long
)

data class AdminIncidentsFilters(
    val status: IncidentStatus? = null,
    val categoryId: Long? = null,
    val priority: Priority? = null,
    val sortOrder: IncidentDateSortOrder = IncidentDateSortOrder.RECENT_DABORD
)

data class AdminIncidentsUiState(
    val isLoading: Boolean = true,
    val items: List<AdminIncidentListItem> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val filters: AdminIncidentsFilters = AdminIncidentsFilters()
)
