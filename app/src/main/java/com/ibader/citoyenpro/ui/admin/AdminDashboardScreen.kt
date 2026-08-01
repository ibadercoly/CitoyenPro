package com.ibader.citoyenpro.ui.admin

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.ui.common.IncidentStatusBadge
import com.ibader.citoyenpro.ui.common.StatCounterCard
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)

// Composable "route" : instancie l'AdminDashboardViewModel et lui délègue
// les compteurs et derniers signalements (Flow Room).
@Composable
fun AdminDashboardRoute(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModel.factory(incidentRepository, categoryRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminDashboardScreen(uiState = uiState, onIncidentClick = onIncidentClick, modifier = modifier)
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun AdminDashboardScreen(
    uiState: AdminDashboardUiState,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        uiState.totalIncidents == 0 -> Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Aucun signalement pour l'instant.", style = MaterialTheme.typography.bodyLarge)
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(text = "Tableau de bord", style = MaterialTheme.typography.headlineSmall)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCounterCard(label = "Total", value = uiState.totalIncidents, modifier = Modifier.weight(1f))
                    uiState.byStatus.forEach { entry ->
                        StatCounterCard(label = entry.status.libelle, value = entry.count, modifier = Modifier.weight(1f))
                    }
                }
            }
            item { HorizontalDivider() }
            item {
                Text(text = "Derniers signalements", style = MaterialTheme.typography.titleMedium)
            }
            items(uiState.recentIncidents, key = { it.id }) { item ->
                DashboardIncidentRow(item = item, onClick = { onIncidentClick(item.id) })
            }
        }
    }
}

@Composable
private fun DashboardIncidentRow(item: AdminIncidentListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = "${item.categoryNom} · ${dateFormat.format(item.dateCreation)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Tableau de bord admin", showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    CitoyenProTheme {
        AdminDashboardScreen(
            uiState = AdminDashboardUiState(
                isLoading = false,
                totalIncidents = 4,
                byStatus = listOf(
                    AdminDashboardStatusCount(IncidentStatus.RECU, 1),
                    AdminDashboardStatusCount(IncidentStatus.EN_COURS, 2),
                    AdminDashboardStatusCount(IncidentStatus.RESOLU, 1),
                    AdminDashboardStatusCount(IncidentStatus.CLOS, 0)
                ),
                recentIncidents = listOf(
                    AdminIncidentListItem(
                        id = 1,
                        titre = "Lampadaire cassé",
                        categoryNom = "Éclairage public",
                        status = IncidentStatus.EN_COURS,
                        priority = Priority.HAUTE,
                        dateCreation = System.currentTimeMillis()
                    ),
                    AdminIncidentListItem(
                        id = 2,
                        titre = "Nid de poule rue des Lilas",
                        categoryNom = "Voirie",
                        status = IncidentStatus.RECU,
                        priority = Priority.MOYENNE,
                        dateCreation = System.currentTimeMillis()
                    )
                )
            ),
            onIncidentClick = {}
        )
    }
}
