package com.ibader.citoyenpro.ui.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Le citoyen connecté est déjà garanti non nul à ce stade (comme MyIncidentsViewModel).
class CitizenProfileViewModel(
    incidentRepository: IncidentRepository,
    userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<CitizenProfileUiState> = run {
        val currentUser = userRepository.currentUser.value

        if (currentUser == null) {
            MutableStateFlow(CitizenProfileUiState(isLoading = false))
        } else {
            incidentRepository.getByCitoyen(currentUser.firebaseUid)
                .map { incidents ->
                    CitizenProfileUiState(
                        isLoading = false,
                        nom = currentUser.nom,
                        email = currentUser.email,
                        role = currentUser.role,
                        totalIncidents = incidents.size,
                        points = currentUser.points
                    )
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CitizenProfileUiState())
        }
    }

    companion object {
        fun factory(incidentRepository: IncidentRepository, userRepository: UserRepository) = viewModelFactory {
            initializer { CitizenProfileViewModel(incidentRepository, userRepository) }
        }
    }
}
