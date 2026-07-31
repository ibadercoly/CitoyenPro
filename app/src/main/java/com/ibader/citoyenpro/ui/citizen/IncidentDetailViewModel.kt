package com.ibader.citoyenpro.ui.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Combine en un seul flux réactif l'incident lui-même, le nom de sa catégorie
// et son historique de statuts : toute mise à jour de l'une de ces sources
// (ex. changement de statut par un admin) se répercute automatiquement ici.
class IncidentDetailViewModel(
    incidentId: Long,
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<IncidentDetailUiState> = combine(
        incidentRepository.observeById(incidentId),
        categoryRepository.getAll(),
        incidentStatusHistoryRepository.getByIncident(incidentId)
    ) { incident, categories, history ->
        IncidentDetailUiState(
            isLoading = false,
            incident = incident,
            categoryNom = categories.find { it.id == incident?.categoryId }?.nom.orEmpty(),
            history = history
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IncidentDetailUiState())

    companion object {
        fun factory(
            incidentId: Long,
            incidentRepository: IncidentRepository,
            categoryRepository: CategoryRepository,
            incidentStatusHistoryRepository: IncidentStatusHistoryRepository
        ) = viewModelFactory {
            initializer {
                IncidentDetailViewModel(
                    incidentId,
                    incidentRepository,
                    categoryRepository,
                    incidentStatusHistoryRepository
                )
            }
        }
    }
}
