package com.ibader.citoyenpro.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.common.IncidentStatusBadge
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)

// Composable "route" : instancie l'AdminIncidentsViewModel et lui délègue la
// liste réactive de tous les signalements (Flow Room, filtrée et triée côté
// ViewModel). La modification d'un signalement se fait depuis son écran de
// détail (onIncidentClick).
@Composable
fun AdminIncidentsRoute(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminIncidentsViewModel = viewModel(
        factory = AdminIncidentsViewModel.factory(incidentRepository, categoryRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminIncidentsScreen(
        uiState = uiState,
        onStatusFilterSelected = viewModel::onStatusFilterSelected,
        onCategoryFilterSelected = viewModel::onCategoryFilterSelected,
        onPriorityFilterSelected = viewModel::onPriorityFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onIncidentClick = onIncidentClick,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun AdminIncidentsScreen(
    uiState: AdminIncidentsUiState,
    onStatusFilterSelected: (IncidentStatus?) -> Unit,
    onCategoryFilterSelected: (Long?) -> Unit,
    onPriorityFilterSelected: (Priority?) -> Unit,
    onSortOrderSelected: (IncidentDateSortOrder) -> Unit,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        AdminIncidentsFiltersSection(
            categories = uiState.categories,
            filters = uiState.filters,
            onStatusFilterSelected = onStatusFilterSelected,
            onCategoryFilterSelected = onCategoryFilterSelected,
            onPriorityFilterSelected = onPriorityFilterSelected,
            onSortOrderSelected = onSortOrderSelected
        )
        HorizontalDivider()

        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            uiState.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucun signalement ne correspond à ces filtres.", style = MaterialTheme.typography.bodyLarge)
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    AdminIncidentRow(item = item, onClick = { onIncidentClick(item.id) })
                }
            }
        }
    }
}

@Composable
private fun AdminIncidentsFiltersSection(
    categories: List<CategoryEntity>,
    filters: AdminIncidentsFilters,
    onStatusFilterSelected: (IncidentStatus?) -> Unit,
    onCategoryFilterSelected: (Long?) -> Unit,
    onPriorityFilterSelected: (Priority?) -> Unit,
    onSortOrderSelected: (IncidentDateSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterRow(label = "Statut") {
            FilterChip(
                selected = filters.status == null,
                onClick = { onStatusFilterSelected(null) },
                label = { Text("Tous") }
            )
            IncidentStatus.entries.forEach { status ->
                FilterChip(
                    selected = filters.status == status,
                    onClick = { onStatusFilterSelected(status) },
                    label = { Text(status.libelle) }
                )
            }
        }

        FilterRow(label = "Catégorie") {
            FilterChip(
                selected = filters.categoryId == null,
                onClick = { onCategoryFilterSelected(null) },
                label = { Text("Toutes") }
            )
            categories.forEach { category ->
                FilterChip(
                    selected = filters.categoryId == category.id,
                    onClick = { onCategoryFilterSelected(category.id) },
                    label = { Text(category.nom) }
                )
            }
        }

        FilterRow(label = "Priorité") {
            FilterChip(
                selected = filters.priority == null,
                onClick = { onPriorityFilterSelected(null) },
                label = { Text("Toutes") }
            )
            Priority.entries.forEach { priority ->
                FilterChip(
                    selected = filters.priority == priority,
                    onClick = { onPriorityFilterSelected(priority) },
                    label = { Text(priority.libelle()) }
                )
            }
        }

        FilterRow(label = "Tri par date") {
            FilterChip(
                selected = filters.sortOrder == IncidentDateSortOrder.RECENT_DABORD,
                onClick = { onSortOrderSelected(IncidentDateSortOrder.RECENT_DABORD) },
                label = { Text("Plus récent d'abord") }
            )
            FilterChip(
                selected = filters.sortOrder == IncidentDateSortOrder.ANCIEN_DABORD,
                onClick = { onSortOrderSelected(IncidentDateSortOrder.ANCIEN_DABORD) },
                label = { Text("Plus ancien d'abord") }
            )
        }
    }
}

@Composable
private fun FilterRow(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
private fun AdminIncidentRow(item: AdminIncidentListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.titre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IncidentStatusBadge(status = item.status)
            }
            Text(
                text = "${item.categoryNom} · Priorité ${item.priority.libelle()} · ${dateFormat.format(item.dateCreation)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Signalements admin", showBackground = true)
@Composable
private fun AdminIncidentsScreenPreview() {
    CitoyenProTheme {
        AdminIncidentsScreen(
            uiState = AdminIncidentsUiState(
                isLoading = false,
                items = listOf(
                    AdminIncidentListItem(
                        id = 1,
                        titre = "Lampadaire cassé",
                        categoryNom = "Éclairage public",
                        status = IncidentStatus.RECU,
                        priority = Priority.HAUTE,
                        dateCreation = System.currentTimeMillis()
                    ),
                    AdminIncidentListItem(
                        id = 2,
                        titre = "Nid de poule rue des Lilas",
                        categoryNom = "Voirie",
                        status = IncidentStatus.EN_COURS,
                        priority = Priority.MOYENNE,
                        dateCreation = System.currentTimeMillis()
                    )
                ),
                categories = listOf(
                    CategoryEntity(id = 1, nom = "Éclairage public", description = ""),
                    CategoryEntity(id = 2, nom = "Voirie", description = "")
                )
            ),
            onStatusFilterSelected = {},
            onCategoryFilterSelected = {},
            onPriorityFilterSelected = {},
            onSortOrderSelected = {},
            onIncidentClick = {}
        )
    }
}
