package com.ibader.citoyenpro.ui.citizen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.LocationRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.common.AppBackground
import com.ibader.citoyenpro.ui.common.AppPrimaryButton
import com.ibader.citoyenpro.ui.common.AppTextField
import com.ibader.citoyenpro.ui.common.AppTextFieldShape
import com.ibader.citoyenpro.ui.common.AppTopBar
import com.ibader.citoyenpro.ui.common.appTextFieldColors
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import com.ibader.citoyenpro.util.createImageCaptureUri

// Composable "route" : instancie le CreateIncidentViewModel et réagit au succès.
@Composable
fun CreateIncidentRoute(
    incidentRepository: IncidentRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    locationRepository: LocationRepository,
    onIncidentCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateIncidentViewModel = viewModel(
        factory = CreateIncidentViewModel.factory(
            incidentRepository,
            incidentStatusHistoryRepository,
            categoryRepository,
            userRepository,
            locationRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSubmitSuccessful) {
        if (uiState.isSubmitSuccessful) onIncidentCreated()
    }

    CreateIncidentScreen(
        uiState = uiState,
        onTitreChange = viewModel::onTitreChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategorySelected = viewModel::onCategorySelected,
        onPrioritySelected = viewModel::onPrioritySelected,
        onPhotoSelected = viewModel::onPhotoSelected,
        onFetchLocation = viewModel::fetchLocation,
        onSubmitClick = viewModel::submit,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIncidentScreen(
    uiState: CreateIncidentUiState,
    onTitreChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onFetchLocation: () -> Unit,
    onSubmitClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = "Signaler un incident",
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                )
            }
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = uiState.titre,
                onValueChange = onTitreChange,
                label = "Titre",
                enabled = !uiState.isLoading,
                isError = uiState.titreError != null,
                errorText = uiState.titreError
            )

            AppTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "Description",
                enabled = !uiState.isLoading,
                isError = uiState.descriptionError != null,
                errorText = uiState.descriptionError,
                singleLine = false,
                minLines = 4
            )

            CategoryDropdown(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                categoryError = uiState.categoryError,
                enabled = !uiState.isLoading,
                onCategorySelected = onCategorySelected
            )

            PhotoPickerSection(
                photoUri = uiState.photoUri,
                onPhotoSelected = onPhotoSelected,
                enabled = !uiState.isLoading
            )

            LocationSection(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                adresse = uiState.adresse,
                isLocating = uiState.isLocating,
                locationError = uiState.locationError,
                enabled = !uiState.isLoading,
                onFetchLocation = onFetchLocation
            )

            Column {
                Text(text = "Priorité", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = uiState.priority == priority,
                            onClick = { onPrioritySelected(priority) },
                            enabled = !uiState.isLoading,
                            label = { Text(priority.libelle()) }
                        )
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AppPrimaryButton(
                text = "Envoyer le signalement",
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading,
                onClick = onSubmitClick
            )
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    categoryError: String?,
    enabled: Boolean,
    onCategorySelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory?.nom ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Catégorie") },
            placeholder = { Text("Sélectionnez une catégorie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = categoryError != null,
            supportingText = { categoryError?.let { Text(it) } },
            enabled = enabled,
            shape = AppTextFieldShape,
            colors = appTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.nom) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Bouton caméra (avec demande de permission CAMERA à l'exécution) + bouton
// galerie (photo picker système, sans permission requise), et aperçu de la
// photo sélectionnée le cas échéant.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PhotoPickerSection(
    photoUri: Uri?,
    onPhotoSelected: (Uri?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (!success) onPhotoSelected(null) }

    fun launchCamera() {
        val uri = createImageCaptureUri(context)
        onPhotoSelected(uri)
        cameraLauncher.launch(uri)
    }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onPhotoSelected(uri) }

    Column(modifier = modifier) {
        Text(text = "Photo", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))

        if (photoUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Aperçu de la photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                IconButton(
                    onClick = { onPhotoSelected(null) },
                    enabled = enabled,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Retirer la photo")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        launchCamera()
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Prendre une photo")
            }
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Choisir dans la galerie")
            }
        }

        if (!cameraPermissionState.status.isGranted && cameraPermissionState.status.shouldShowRationale) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "L'accès à la caméra est nécessaire pour prendre une photo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Bouton de géolocalisation (avec demande de permission de localisation à
// l'exécution), qui déclenche la récupération de la position via
// FusedLocationProviderClient puis le géocodage inverse côté ViewModel, et
// affiche la position (adresse si résolue, sinon coordonnées) une fois connue.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationSection(
    latitude: Double?,
    longitude: Double?,
    adresse: String,
    isLocating: Boolean,
    locationError: String?,
    enabled: Boolean,
    onFetchLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { results -> if (results.values.any { it }) onFetchLocation() }

    Column(modifier = modifier) {
        Text(text = "Localisation", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                    onFetchLocation()
                } else {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            },
            enabled = enabled && !isLocating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (latitude != null) "Actualiser ma position" else "Utiliser ma position actuelle")
            }
        }

        if (latitude != null && longitude != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = adresse.ifBlank { "Lat : %.5f, Lng : %.5f".format(latitude, longitude) },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        locationError?.let { message ->
            Spacer(Modifier.height(4.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        if (locationPermissionsState.permissions.none { it.status.isGranted } &&
            locationPermissionsState.permissions.any { it.status.shouldShowRationale }
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "L'accès à la position est nécessaire pour géolocaliser le signalement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(name = "Nouveau signalement", showBackground = true)
@Composable
private fun CreateIncidentScreenPreview() {
    CitoyenProTheme {
        CreateIncidentScreen(
            uiState = CreateIncidentUiState(
                titre = "Lampadaire cassé",
                description = "Le lampadaire au coin de la rue est hors service depuis une semaine.",
                categories = listOf(
                    CategoryEntity(id = 1, nom = "Éclairage public", description = ""),
                    CategoryEntity(id = 2, nom = "Voirie", description = "")
                ),
                selectedCategoryId = 1,
                priority = Priority.HAUTE
            ),
            onTitreChange = {},
            onDescriptionChange = {},
            onCategorySelected = {},
            onPrioritySelected = {},
            onPhotoSelected = {},
            onFetchLocation = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Nouveau signalement — erreurs", showBackground = true)
@Composable
private fun CreateIncidentScreenErrorPreview() {
    CitoyenProTheme {
        CreateIncidentScreen(
            uiState = CreateIncidentUiState(
                titreError = "Le titre est requis",
                descriptionError = "La description est requise",
                categoryError = "Sélectionnez une catégorie"
            ),
            onTitreChange = {},
            onDescriptionChange = {},
            onCategorySelected = {},
            onPrioritySelected = {},
            onPhotoSelected = {},
            onFetchLocation = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}
