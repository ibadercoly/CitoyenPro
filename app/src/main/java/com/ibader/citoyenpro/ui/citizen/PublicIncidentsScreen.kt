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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.ibader.citoyenpro.data.repository.IncidentVoteRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.common.IncidentStatusBadge
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)

// Composable "route" : instancie le PublicIncidentsViewModel et lui délègue
// le fil public (Flow Room, tous les signalements) trié par votes ainsi que
// l'action de soutien.
@Composable
fun PublicIncidentsRoute(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    incidentVoteRepository: IncidentVoteRepository,
    userRepository: UserRepository,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PublicIncidentsViewModel = viewModel(
        factory = PublicIncidentsViewModel.factory(
            incidentRepository,
            categoryRepository,
            incidentVoteRepository,
            userRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PublicIncidentsScreen(
        uiState = uiState,
        onSupportClick = viewModel::onSupportClick,
        onIncidentClick = onIncidentClick,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun PublicIncidentsScreen(
    uiState: PublicIncidentsUiState,
    onSupportClick: (Long) -> Unit,
    onIncidentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        uiState.items.isEmpty() -> Box(
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
            items(uiState.items, key = { it.id }) { item ->
                PublicIncidentRow(
                    item = item,
                    onClick = { onIncidentClick(item.id) },
                    onSupportClick = { onSupportClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun PublicIncidentRow(
    item: PublicIncidentListItem,
    onClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.voteCount <= 1) "${item.voteCount} soutien" else "${item.voteCount} soutiens",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = onSupportClick, enabled = !item.hasVoted) {
                    Icon(
                        Icons.Filled.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (item.hasVoted) "Soutenu" else "Soutenir")
                }
            }
        }
    }
}

@Preview(name = "Signalements publics", showBackground = true)
@Composable
private fun PublicIncidentsScreenPreview() {
    CitoyenProTheme {
        PublicIncidentsScreen(
            uiState = PublicIncidentsUiState(
                isLoading = false,
                items = listOf(
                    PublicIncidentListItem(
                        id = 1,
                        titre = "Nid de poule rue des Lilas",
                        categoryNom = "Voirie",
                        status = IncidentStatus.EN_COURS,
                        priority = Priority.HAUTE,
                        dateCreation = System.currentTimeMillis(),
                        voteCount = 12,
                        hasVoted = true
                    ),
                    PublicIncidentListItem(
                        id = 2,
                        titre = "Lampadaire cassé",
                        categoryNom = "Éclairage public",
                        status = IncidentStatus.RECU,
                        priority = Priority.MOYENNE,
                        dateCreation = System.currentTimeMillis(),
                        voteCount = 3,
                        hasVoted = false
                    )
                )
            ),
            onSupportClick = {},
            onIncidentClick = {}
        )
    }
}