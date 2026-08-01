package com.ibader.citoyenpro.ui.admin

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
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Composable "route" : instancie l'AdminUsersViewModel et lui délègue la
// liste réactive de tous les comptes (Flow Room).
@Composable
fun AdminUsersRoute(
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    viewModel: AdminUsersViewModel = viewModel(factory = AdminUsersViewModel.factory(userRepository))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminUsersScreen(uiState = uiState, modifier = modifier)
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun AdminUsersScreen(uiState: AdminUsersUiState, modifier: Modifier = Modifier) {
    when {
        uiState.isLoading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        uiState.items.isEmpty() -> Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Aucun compte pour l'instant.", style = MaterialTheme.typography.bodyLarge)
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(text = "Comptes", style = MaterialTheme.typography.headlineSmall)
            }
            items(uiState.items, key = { it.id }) { item ->
                UserRow(item = item)
            }
        }
    }
}

@Composable
private fun UserRow(item: AdminUserListItem, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = item.nom, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.role.libelle(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Comptes admin", showBackground = true)
@Composable
private fun AdminUsersScreenPreview() {
    CitoyenProTheme {
        AdminUsersScreen(
            uiState = AdminUsersUiState(
                isLoading = false,
                items = listOf(
                    AdminUserListItem(id = 1, nom = "Administrateur", email = "admin@citoyenpro.local", role = UserRole.ADMIN),
                    AdminUserListItem(id = 2, nom = "Awa Ndiaye", email = "awa@example.com", role = UserRole.CITOYEN)
                )
            )
        )
    }
}
