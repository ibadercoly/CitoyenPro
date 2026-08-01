package com.ibader.citoyenpro.ui.admin

// colorSlot indexe dans la palette catégorielle (statut, catégorie) ou dans
// la rampe séquentielle (priorité, où l'ordre porte la sévérité croissante) ;
// -1 signifie "couleur neutre" (utilisé pour le compartiment "Autres").
data class StatEntry(
    val label: String,
    val count: Int,
    val colorSlot: Int
)

data class AdminStatsUiState(
    val isLoading: Boolean = true,
    val totalIncidents: Int = 0,
    val byStatus: List<StatEntry> = emptyList(),
    val byCategory: List<StatEntry> = emptyList(),
    val byPriority: List<StatEntry> = emptyList()
)
