package com.ibader.citoyenpro.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// La modification d'un signalement (statut, service affecté) se fait depuis
// son écran de détail (AdminIncidentDetailViewModel) ; cet écran ne fait que
// lister, filtrer et trier.
class AdminIncidentsViewModel(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(AdminIncidentsFilters())

    val uiState: StateFlow<AdminIncidentsUiState> = combine(
        incidentRepository.getAll(),
        categoryRepository.getAll(),
        _filters
    ) { incidents, categories, filters ->
        val sorted = when (filters.sortOrder) {
            IncidentDateSortOrder.RECENT_DABORD -> incidents.sortedByDescending { it.dateCreation }
            IncidentDateSortOrder.ANCIEN_DABORD -> incidents.sortedBy { it.dateCreation }
        }
        val filtered = sorted
            .filter { filters.status == null || it.status == filters.status }
            .filter { filters.categoryId == null || it.categoryId == filters.categoryId }
            .filter { filters.priority == null || it.priority == filters.priority }

        AdminIncidentsUiState(
            isLoading = false,
            items = filtered.map { incident ->
                AdminIncidentListItem(
                    id = incident.id,
                    titre = incident.titre,
                    categoryNom = categories.find { it.id == incident.categoryId }?.nom.orEmpty(),
                    status = incident.status,
                    priority = incident.priority,
                    dateCreation = incident.dateCreation
                )
            },
            categories = categories,
            filters = filters
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminIncidentsUiState())

    fun onStatusFilterSelected(status: IncidentStatus?) {
        _filters.update { it.copy(status = status) }
    }

    fun onCategoryFilterSelected(categoryId: Long?) {
        _filters.update { it.copy(categoryId = categoryId) }
    }

    fun onPriorityFilterSelected(priority: Priority?) {
        _filters.update { it.copy(priority = priority) }
    }

    fun onSortOrderSelected(order: IncidentDateSortOrder) {
        _filters.update { it.copy(sortOrder = order) }
    }

    companion object {
        fun factory(
            incidentRepository: IncidentRepository,
            categoryRepository: CategoryRepository
        ) = viewModelFactory {
            initializer {
                AdminIncidentsViewModel(incidentRepository, categoryRepository)
            }
        }
    }
}
