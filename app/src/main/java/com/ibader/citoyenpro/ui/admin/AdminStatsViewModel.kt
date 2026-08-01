package com.ibader.citoyenpro.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.domain.model.libelle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val MAX_CATEGORY_BARS = 8

// Agrège les signalements par statut, catégorie et priorité pour l'écran de
// statistiques admin. Statuts et priorités listent toutes leurs valeurs même
// à zéro (comptage complet, légende stable) ; les catégories sont triées par
// nombre décroissant et repliées au-delà des 8 premières dans une entrée
// "Autres" pour rester lisible même si l'admin en crée beaucoup.
class AdminStatsViewModel(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<AdminStatsUiState> = combine(
        incidentRepository.getAll(),
        categoryRepository.getAll()
    ) { incidents, categories ->
        val byStatus = IncidentStatus.entries.mapIndexed { index, status ->
            StatEntry(
                label = status.libelle,
                count = incidents.count { it.status == status },
                colorSlot = index
            )
        }

        val byPriority = Priority.entries.mapIndexed { index, priority ->
            StatEntry(
                label = priority.libelle(),
                count = incidents.count { it.priority == priority },
                colorSlot = index
            )
        }

        val categoryCounts = incidents
            .groupingBy { it.categoryId }
            .eachCount()
            .mapNotNull { (categoryId, count) ->
                categories.find { it.id == categoryId }?.let { it.nom to count }
            }
            .sortedByDescending { it.second }

        val topCategories = categoryCounts.take(MAX_CATEGORY_BARS).mapIndexed { index, (nom, count) ->
            StatEntry(label = nom, count = count, colorSlot = index)
        }
        val otherCount = categoryCounts.drop(MAX_CATEGORY_BARS).sumOf { it.second }
        val byCategory = if (otherCount > 0) {
            topCategories + StatEntry(label = "Autres", count = otherCount, colorSlot = -1)
        } else {
            topCategories
        }

        AdminStatsUiState(
            isLoading = false,
            totalIncidents = incidents.size,
            byStatus = byStatus,
            byCategory = byCategory,
            byPriority = byPriority
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminStatsUiState())

    companion object {
        fun factory(incidentRepository: IncidentRepository, categoryRepository: CategoryRepository) =
            viewModelFactory {
                initializer { AdminStatsViewModel(incidentRepository, categoryRepository) }
            }
    }
}
