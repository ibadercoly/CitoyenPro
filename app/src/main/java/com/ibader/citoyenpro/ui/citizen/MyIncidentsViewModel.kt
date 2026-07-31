package com.ibader.citoyenpro.ui.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Le citoyen connecté est déjà garanti non nul à ce stade (écran atteignable
// uniquement depuis l'espace citoyen, lui-même conditionné par la session).
class MyIncidentsViewModel(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<MyIncidentsUiState> = run {
        val citoyenId = userRepository.currentUser.value?.id

        if (citoyenId == null) {
            MutableStateFlow(MyIncidentsUiState(isLoading = false))
        } else {
            combine(
                incidentRepository.getByCitoyen(citoyenId),
                categoryRepository.getAll()
            ) { incidents, categories ->
                MyIncidentsUiState(
                    isLoading = false,
                    items = incidents.map { incident ->
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
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyIncidentsUiState())
        }
    }

    companion object {
        fun factory(
            incidentRepository: IncidentRepository,
            categoryRepository: CategoryRepository,
            userRepository: UserRepository
        ) = viewModelFactory {
            initializer {
                MyIncidentsViewModel(incidentRepository, categoryRepository, userRepository)
            }
        }
    }
}
