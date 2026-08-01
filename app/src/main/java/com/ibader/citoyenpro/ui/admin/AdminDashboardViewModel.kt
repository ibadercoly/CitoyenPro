package com.ibader.citoyenpro.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val RECENT_INCIDENTS_LIMIT = 5

// Vue d'ensemble rapide (compteurs + derniers signalements) ; le détail par
// catégorie/priorité avec graphiques est du ressort d'AdminStatsScreen.
// getAll() trie déjà par date décroissante (cf. IncidentDao) : les N
// premiers éléments sont donc directement les plus récents.
class AdminDashboardViewModel(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<AdminDashboardUiState> = combine(
        incidentRepository.getAll(),
        categoryRepository.getAll()
    ) { incidents, categories ->
        AdminDashboardUiState(
            isLoading = false,
            totalIncidents = incidents.size,
            byStatus = IncidentStatus.entries.map { status ->
                AdminDashboardStatusCount(status = status, count = incidents.count { it.status == status })
            },
            recentIncidents = incidents.take(RECENT_INCIDENTS_LIMIT).map { incident ->
                AdminIncidentListItem(
                    id = incident.id,
                    titre = incident.titre,
                    categoryNom = categories.find { it.id == incident.categoryId }?.nom.orEmpty(),
                    status = incident.status,
                    priority = incident.priority,
                    dateCreation = incident.dateCreation
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminDashboardUiState())

    companion object {
        fun factory(incidentRepository: IncidentRepository, categoryRepository: CategoryRepository) =
            viewModelFactory {
                initializer { AdminDashboardViewModel(incidentRepository, categoryRepository) }
            }
    }
}
