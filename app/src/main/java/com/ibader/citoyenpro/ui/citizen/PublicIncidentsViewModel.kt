package com.ibader.citoyenpro.ui.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentVoteRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Fil public : tous les signalements (pas seulement ceux du citoyen
// connecté), triés par nombre de votes décroissant — à égalité, l'ordre par
// date décroissante déjà appliqué par IncidentDao.getAll() est préservé
// (tri stable). Le citoyen connecté est garanti non nul ici (comme les
// autres écrans citoyen).
class PublicIncidentsViewModel(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    private val incidentVoteRepository: IncidentVoteRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val citoyenId = userRepository.currentUser.value?.id

    val uiState: StateFlow<PublicIncidentsUiState> = run {
        val id = citoyenId

        if (id == null) {
            MutableStateFlow(PublicIncidentsUiState(isLoading = false))
        } else {
            combine(
                incidentRepository.getAll(),
                categoryRepository.getAll(),
                incidentVoteRepository.getVoteCounts(),
                incidentVoteRepository.getVotedIncidentIds(id)
            ) { incidents, categories, voteCounts, votedIds ->
                val countByIncident = voteCounts.associate { it.incidentId to it.count }
                val votedSet = votedIds.toSet()

                PublicIncidentsUiState(
                    isLoading = false,
                    items = incidents
                        .map { incident ->
                            PublicIncidentListItem(
                                id = incident.id,
                                titre = incident.titre,
                                categoryNom = categories.find { it.id == incident.categoryId }?.nom.orEmpty(),
                                status = incident.status,
                                priority = incident.priority,
                                dateCreation = incident.dateCreation,
                                voteCount = countByIncident[incident.id] ?: 0,
                                hasVoted = incident.id in votedSet
                            )
                        }
                        .sortedByDescending { it.voteCount }
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PublicIncidentsUiState())
        }
    }

    fun onSupportClick(incidentId: Long) {
        val id = citoyenId ?: return
        viewModelScope.launch {
            incidentVoteRepository.vote(incidentId, id)
        }
    }

    companion object {
        fun factory(
            incidentRepository: IncidentRepository,
            categoryRepository: CategoryRepository,
            incidentVoteRepository: IncidentVoteRepository,
            userRepository: UserRepository
        ) = viewModelFactory {
            initializer {
                PublicIncidentsViewModel(incidentRepository, categoryRepository, incidentVoteRepository, userRepository)
            }
        }
    }
}