package com.ibader.citoyenpro.ui.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.ui.common.AppBackground
import com.ibader.citoyenpro.ui.common.AppCard
import com.ibader.citoyenpro.ui.common.AppPrimaryButton
import com.ibader.citoyenpro.ui.common.IncidentStatusBadge
import com.ibader.citoyenpro.ui.common.StatCounterCard
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)

// Composable "route" : instancie le CitizenHomeViewModel et lui délègue les
// compteurs et derniers signalements du citoyen connecté (Flow Room).
@Composable
fun CitizenHomeRoute(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    onCreateIncidentClick: () -> Unit,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitizenHomeViewModel = viewModel(
        factory = CitizenHomeViewModel.factory(incidentRepository, categoryRepository, userRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CitizenHomeScreen(
        uiState = uiState,
        onCreateIncidentClick = onCreateIncidentClick,
        onIncidentClick = onIncidentClick,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun CitizenHomeScreen(
    uiState: CitizenHomeUiState,
    onCreateIncidentClick: () -> Unit,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AppBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(text = "Bonjour, ${uiState.userName}", style = MaterialTheme.typography.headlineSmall)
            }
            item {
                AppPrimaryButton(
                    text = "Signaler un incident",
                    isLoading = false,
                    enabled = true,
                    onClick = onCreateIncidentClick
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCounterCard(label = "Total", value = uiState.totalIncidents, modifier = Modifier.weight(1f))
                    uiState.byStatus.forEach { entry ->
                        StatCounterCard(label = entry.status.libelle, value = entry.count, modifier = Modifier.weight(1f))
                    }
                }
            }
            if (uiState.recentIncidents.isNotEmpty()) {
                item {
                    Text(text = "Vos derniers signalements", style = MaterialTheme.typography.titleMedium)
                }
                items(uiState.recentIncidents, key = { it.id }) { item ->
                    HomeIncidentRow(item = item, onClick = { onIncidentClick(item.id) })
                }
            }
        }
    }
}

@Composable
private fun HomeIncidentRow(item: IncidentListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        onClick = onClick,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
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

@Preview(name = "Accueil citoyen", showBackground = true)
@Composable
private fun CitizenHomeScreenPreview() {
    CitoyenProTheme {
        CitizenHomeScreen(
            uiState = CitizenHomeUiState(
                isLoading = false,
                userName = "Awa Ndiaye",
                totalIncidents = 3,
                byStatus = listOf(
                    CitizenHomeStatusCount(IncidentStatus.RECU, 1),
                    CitizenHomeStatusCount(IncidentStatus.EN_COURS, 1),
                    CitizenHomeStatusCount(IncidentStatus.RESOLU, 1),
                    CitizenHomeStatusCount(IncidentStatus.CLOS, 0)
                ),
                recentIncidents = listOf(
                    IncidentListItem(
                        id = 1,
                        titre = "Lampadaire cassé",
                        categoryNom = "Éclairage public",
                        status = IncidentStatus.EN_COURS,
                        priority = Priority.HAUTE,
                        dateCreation = System.currentTimeMillis()
                    )
                )
            ),
            onCreateIncidentClick = {},
            onIncidentClick = {}
        )
    }
}
