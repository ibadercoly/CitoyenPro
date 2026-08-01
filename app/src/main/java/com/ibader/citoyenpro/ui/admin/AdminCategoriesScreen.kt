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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Composable "route" : instancie l'AdminCategoriesViewModel et lui délègue la
// liste réactive des catégories (Flow Room) ainsi que les actions CRUD.
@Composable
fun AdminCategoriesRoute(
    categoryRepository: CategoryRepository,
    modifier: Modifier = Modifier,
    viewModel: AdminCategoriesViewModel = viewModel(
        factory = AdminCategoriesViewModel.factory(categoryRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminCategoriesScreen(
        uiState = uiState,
        onAddClick = viewModel::onAddClick,
        onEditClick = viewModel::onEditClick,
        onDeleteClick = viewModel::onDeleteClick,
        onFormNomChanged = viewModel::onFormNomChanged,
        onFormDescriptionChanged = viewModel::onFormDescriptionChanged,
        onDismissForm = viewModel::onDismissForm,
        onSaveClick = viewModel::onSaveClick,
        onDismissDeleteConfirmation = viewModel::onDismissDeleteConfirmation,
        onConfirmDelete = viewModel::onConfirmDelete,
        onErrorDismissed = viewModel::onErrorDismissed,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun AdminCategoriesScreen(
    uiState: AdminCategoriesUiState,
    onAddClick: () -> Unit,
    onEditClick: (CategoryEntity) -> Unit,
    onDeleteClick: (CategoryEntity) -> Unit,
    onFormNomChanged: (String) -> Unit,
    onFormDescriptionChanged: (String) -> Unit,
    onDismissForm: () -> Unit,
    onSaveClick: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onConfirmDelete: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une catégorie")
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

            uiState.categories.isEmpty() -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucune catégorie pour l'instant.", style = MaterialTheme.typography.bodyLarge)
            }

            else -> LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEditClick = { onEditClick(category) },
                        onDeleteClick = { onDeleteClick(category) }
                    )
                }
            }
        }
    }

    uiState.formState?.let { form ->
        CategoryFormDialog(
            form = form,
            onNomChanged = onFormNomChanged,
            onDescriptionChanged = onFormDescriptionChanged,
            onDismiss = onDismissForm,
            onSave = onSaveClick
        )
    }

    uiState.categoryPendingDeletion?.let { category ->
        DeleteCategoryConfirmationDialog(
            category = category,
            onDismiss = onDismissDeleteConfirmation,
            onConfirm = onConfirmDelete
        )
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onErrorDismissed,
            confirmButton = {
                TextButton(onClick = onErrorDismissed) { Text("OK") }
            },
            title = { Text("Suppression impossible") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.nom, style = MaterialTheme.typography.titleMedium)
                if (category.description.isNotBlank()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Modifier ${category.nom}")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer ${category.nom}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormDialog(
    form: CategoryFormState,
    onNomChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(if (form.id == null) "Nouvelle catégorie" else "Modifier la catégorie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.nom,
                    onValueChange = onNomChanged,
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.description,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = form.isValid) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun DeleteCategoryConfirmationDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la catégorie ?") },
        text = { Text("« ${category.nom} » sera définitivement supprimée.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Supprimer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Preview(name = "Catégories admin", showBackground = true)
@Composable
private fun AdminCategoriesScreenPreview() {
    CitoyenProTheme {
        AdminCategoriesScreen(
            uiState = AdminCategoriesUiState(
                isLoading = false,
                categories = listOf(
                    CategoryEntity(id = 1, nom = "Éclairage public", description = "Lampadaires, feux tricolores"),
                    CategoryEntity(id = 2, nom = "Voirie", description = "Nids de poule, chaussée")
                )
            ),
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onFormNomChanged = {},
            onFormDescriptionChanged = {},
            onDismissForm = {},
            onSaveClick = {},
            onDismissDeleteConfirmation = {},
            onConfirmDelete = {},
            onErrorDismissed = {}
        )
    }
}
