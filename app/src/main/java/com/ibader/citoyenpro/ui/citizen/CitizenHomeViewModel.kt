package com.ibader.citoyenpro.ui.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val RECENT_INCIDENTS_LIMIT = 3

// Le citoyen connecté est déjà garanti non nul à ce stade (comme MyIncidentsViewModel).
// getByCitoyen trie déjà par date décroissante (cf. IncidentDao) : les N
// premiers éléments sont donc directement les plus récents.
class CitizenHomeViewModel(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<CitizenHomeUiState> = run {
        val currentUser = userRepository.currentUser.value

        if (currentUser == null) {
            MutableStateFlow(CitizenHomeUiState(isLoading = false))
        } else {
            combine(
                incidentRepository.getByCitoyen(currentUser.firebaseUid),
                categoryRepository.getAll()
            ) { incidents, categories ->
                CitizenHomeUiState(
                    isLoading = false,
                    userName = currentUser.nom,
                    totalIncidents = incidents.size,
                    byStatus = IncidentStatus.entries.map { status ->
                        CitizenHomeStatusCount(status = status, count = incidents.count { it.status == status })
                    },
                    recentIncidents = incidents.take(RECENT_INCIDENTS_LIMIT).map { incident ->
                        IncidentListItem(
                            id = incident.id,
                            titre = incident.titre,
                            categoryNom = categories.find { it.id == incident.categoryId }?.nom.orEmpty(),
                            status = incident.status,
                            priority = incident.priority,
                            dateCreation = incident.dateCreation
                        )
                    }
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CitizenHomeUiState())
        }
    }

    companion object {
        fun factory(
            incidentRepository: IncidentRepository,
            categoryRepository: CategoryRepository,
            userRepository: UserRepository
        ) = viewModelFactory {
            initializer { CitizenHomeViewModel(incidentRepository, categoryRepository, userRepository) }
        }
    }
}
