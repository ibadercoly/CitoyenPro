package com.ibader.citoyenpro.ui.citizen

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.common.IncidentStatusBadge
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)

// Composable "route" : instancie le MyIncidentsViewModel et lui délègue la
// liste réactive des signalements du citoyen connecté (Flow Room).
@Composable
fun MyIncidentsRoute(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    showConfirmation: Boolean,
    onConfirmationShown: () -> Unit,
    onCreateIncidentClick: () -> Unit,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyIncidentsViewModel = viewModel(
        factory = MyIncidentsViewModel.factory(incidentRepository, categoryRepository, userRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyIncidentsScreen(
        uiState = uiState,
        showConfirmation = showConfirmation,
        onConfirmationShown = onConfirmationShown,
        onCreateIncidentClick = onCreateIncidentClick,
        onIncidentClick = onIncidentClick,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun MyIncidentsScreen(
    uiState: MyIncidentsUiState,
    showConfirmation: Boolean,
    onConfirmationShown: () -> Unit,
    onCreateIncidentClick: () -> Unit,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            snackbarHostState.showSnackbar("Signalement envoyé avec succès")
            onConfirmationShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateIncidentClick) {
                Icon(Icons.Filled.Add, contentDescription = "Signaler un incident")
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.items.isEmpty() -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vous n'avez encore signalé aucun incident.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    IncidentListRow(item = item, onClick = { onIncidentClick(item.id) })
                }
            }
        }
    }
}

@Composable
private fun IncidentListRow(item: IncidentListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
                Text(
                    text = item.titre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IncidentStatusBadge(status = item.status)
            }
            Text(
                text = item.categoryNom,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Priorité : ${item.priority.libelle()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(item.dateCreation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Mes signalements", showBackground = true)
@Composable
private fun MyIncidentsScreenPreview() {
    CitoyenProTheme {
        MyIncidentsScreen(
            uiState = MyIncidentsUiState(
                isLoading = false,
                items = listOf(
                    IncidentListItem(
                        id = 1,
                        titre = "Lampadaire cassé",
                        categoryNom = "Éclairage public",
                        status = IncidentStatus.EN_COURS,
                        priority = Priority.HAUTE,
                        dateCreation = System.currentTimeMillis()
                    ),
                    IncidentListItem(
                        id = 2,
                        titre = "Nid de poule rue des Lilas",
                        categoryNom = "Voirie",
                        status = IncidentStatus.RECU,
                        priority = Priority.MOYENNE,
                        dateCreation = System.currentTimeMillis()
                    )
                )
            ),
            showConfirmation = false,
            onConfirmationShown = {},
            onCreateIncidentClick = {},
            onIncidentClick = {}
        )
    }
}

@Preview(name = "Mes signalements — vide", showBackground = true)
@Composable
private fun MyIncidentsScreenEmptyPreview() {
    CitoyenProTheme {
        MyIncidentsScreen(
            uiState = MyIncidentsUiState(isLoading = false, items = emptyList()),
            showConfirmation = false,
            onConfirmationShown = {},
            onCreateIncidentClick = {},
            onIncidentClick = {}
        )
    }
}
