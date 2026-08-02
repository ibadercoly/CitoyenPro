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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Combine en un seul flux réactif l'incident, sa catégorie, son historique
// (comme côté citoyen) et l'état local de la boîte de dialogue de
// suppression, et expose les actions admin : changer le statut, affecter un
// service compétent, et supprimer l'incident. Les deux premières passent par
// IncidentUpdateService qui persiste dans Room, journalise l'historique et
// notifie le citoyen ; la suppression passe directement par
// IncidentRepository (pas de notification/historique pertinents une fois
// l'incident supprimé).
class AdminIncidentDetailViewModel(
    private val incidentId: Long,
    private val incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    private val incidentUpdateService: IncidentUpdateService
) : ViewModel() {

    private data class DialogState(val showDeleteConfirmation: Boolean = false, val isDeleted: Boolean = false)

    private val _dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<AdminIncidentDetailUiState> = combine(
        incidentRepository.observeById(incidentId),
        categoryRepository.getAll(),
        incidentStatusHistoryRepository.getByIncident(incidentId),
        _dialogState
    ) { incident, categories, history, dialogState ->
        AdminIncidentDetailUiState(
            isLoading = false,
            incident = incident,
            categoryNom = categories.find { it.id == incident?.categoryId }?.nom.orEmpty(),
            history = history,
            showDeleteConfirmation = dialogState.showDeleteConfirmation,
            isDeleted = dialogState.isDeleted
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

    fun onDeleteClick() {
        _dialogState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDismissDeleteConfirmation() {
        _dialogState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun onConfirmDelete() {
        val incident = uiState.value.incident ?: return
        viewModelScope.launch {
            incidentRepository.delete(incident)
            _dialogState.update { it.copy(showDeleteConfirmation = false, isDeleted = true) }
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
