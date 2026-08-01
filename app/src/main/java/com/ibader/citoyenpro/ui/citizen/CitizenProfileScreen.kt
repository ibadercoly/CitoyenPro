package com.ibader.citoyenpro.ui.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Composable "route" : instancie le CitizenProfileViewModel et lui délègue
// les informations du citoyen connecté (session + Flow Room pour le compteur).
@Composable
fun CitizenProfileRoute(
    incidentRepository: IncidentRepository,
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    viewModel: CitizenProfileViewModel = viewModel(
        factory = CitizenProfileViewModel.factory(incidentRepository, userRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CitizenProfileScreen(uiState = uiState, modifier = modifier)
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun CitizenProfileScreen(uiState: CitizenProfileUiState, modifier: Modifier = Modifier) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Profil", style = MaterialTheme.typography.headlineSmall)
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileField(label = "Nom", value = uiState.nom)
                ProfileField(label = "Email", value = uiState.email)
                ProfileField(label = "Rôle", value = uiState.role.libelle())
                ProfileField(label = "Signalements envoyés", value = uiState.totalIncidents.toString())
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(name = "Profil citoyen", showBackground = true)
@Composable
private fun CitizenProfileScreenPreview() {
    CitoyenProTheme {
        CitizenProfileScreen(
            uiState = CitizenProfileUiState(
                isLoading = false,
                nom = "Awa Ndiaye",
                email = "awa@example.com",
                role = UserRole.CITOYEN,
                totalIncidents = 3
            )
        )
    }
}
