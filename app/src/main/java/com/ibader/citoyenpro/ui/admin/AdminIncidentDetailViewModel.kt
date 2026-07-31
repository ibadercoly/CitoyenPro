package com.ibader.citoyenpro.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.IncidentUpdateService
import com.ibader.citoyenpro.domain.model.IncidentStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Combine en un seul flux réactif l'incident, sa catégorie et son historique
// (comme côté citoyen), et expose les deux actions admin : changer le statut
// et affecter/retirer un service compétent. Les deux passent par
// IncidentUpdateService qui persiste dans Room, journalise l'historique et
// notifie le citoyen.
class AdminIncidentDetailViewModel(
    private val incidentId: Long,
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    private val incidentUpdateService: IncidentUpdateService
) : ViewModel() {

    val uiState: StateFlow<AdminIncidentDetailUiState> = combine(
        incidentRepository.observeById(incidentId),
        categoryRepository.getAll(),
        incidentStatusHistoryRepository.getByIncident(incidentId)
    ) { incident, categories, history ->
        AdminIncidentDetailUiState(
            isLoading = false,
            incident = incident,
            categoryNom = categories.find { it.id == incident?.categoryId }?.nom.orEmpty(),
            history = history
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminIncidentDetailUiState())

    fun onStatusSelected(status: IncidentStatus) {
        viewModelScope.launch {
            incidentUpdateService.updateStatus(incidentId, status)
        }
    }

    fun onServiceAssigned(service: String) {
        viewModelScope.launch {
            incidentUpdateService.assignService(incidentId, service.trim().ifBlank { null })
        }
    }

    companion object {
        fun factory(
            incidentId: Long,
            incidentRepository: IncidentRepository,
            categoryRepository: CategoryRepository,
            incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
            incidentUpdateService: IncidentUpdateService
        ) = viewModelFactory {
            initializer {
                AdminIncidentDetailViewModel(
                    incidentId,
                    incidentRepository,
                    categoryRepository,
                    incidentStatusHistoryRepository,
                    incidentUpdateService
                )
            }
        }
    }
}
