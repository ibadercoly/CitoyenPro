package com.ibader.citoyenpro.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.IncidentUpdateService
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.ui.common.IncidentStatusBadge
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.text.SimpleDateFormat
import java.util.Locale
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)

// Composable "route" : instancie l'AdminIncidentDetailViewModel pour
// l'incident désigné par son id et lui délègue le détail réactif ainsi que
// les deux actions admin (changer le statut, affecter un service).
@Composable
fun AdminIncidentDetailRoute(
    incidentId: Long,
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    incidentUpdateService: IncidentUpdateService,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminIncidentDetailViewModel = viewModel(
        factory = AdminIncidentDetailViewModel.factory(
            incidentId,
            incidentRepository,
            categoryRepository,
            incidentStatusHistoryRepository,
            incidentUpdateService
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminIncidentDetailScreen(
        uiState = uiState,
        onStatusSelected = viewModel::onStatusSelected,
        onServiceAssigned = viewModel::onServiceAssigned,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIncidentDetailScreen(
    uiState: AdminIncidentDetailUiState,
    onStatusSelected: (IncidentStatus) -> Unit,
    onServiceAssigned: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Détail du signalement") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        val incident = uiState.incident

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            incident == null -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Signalement introuvable", style = MaterialTheme.typography.bodyLarge)
            }

            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                incident.photoUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Photo du signalement",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = incident.titre, style = MaterialTheme.typography.headlineSmall)
                        IncidentStatusBadge(status = incident.status)
                    }
                    Text(
                        text = "${uiState.categoryNom} · Priorité ${incident.priority.libelle()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Dernière mise à jour : ${dateTimeFormat.format(incident.dateMaj)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(text = incident.description, style = MaterialTheme.typography.bodyLarge)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Changer le statut", style = MaterialTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        IncidentStatus.entries.forEach { status ->
                            FilterChip(
                                selected = incident.status == status,
                                onClick = { onStatusSelected(status) },
                                label = { Text(status.libelle) }
                            )
                        }
                    }
                }

                ServiceAssignmentSection(
                    currentService = incident.serviceAffecte,
                    onServiceAssigned = onServiceAssigned
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Position", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = incident.adresse.ifBlank {
                            "Lat : %.5f, Lng : %.5f".format(incident.latitude, incident.longitude)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IncidentLocationMap(
                        latitude = incident.latitude,
                        longitude = incident.longitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Historique", style = MaterialTheme.typography.titleMedium)
                    if (uiState.history.isEmpty()) {
                        Text(
                            text = "Aucun changement pour l'instant.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.history.forEachIndexed { index, entry ->
                            StatusHistoryRow(entry)
                            if (index != uiState.history.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// Champ libre (aucune liste de services prédéfinie dans le domaine pour
// l'instant) avec enregistrement explicite plutôt qu'à chaque frappe, pour
// éviter d'écrire dans Room et de notifier le citoyen à chaque caractère saisi.
// Re-clée sur la valeur persistée : la saisie locale n'est réinitialisée que
// lorsque la valeur enregistrée change réellement (premier chargement, ou
// après un enregistrement réussi), pas à chaque recomposition.
@Composable
private fun ServiceAssignmentSection(
    currentService: String?,
    onServiceAssigned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var serviceInput by remember(currentService) { mutableStateOf(currentService.orEmpty()) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Service compétent", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = serviceInput,
            onValueChange = { serviceInput = it },
            label = { Text("Service affecté") },
            placeholder = { Text("Ex. Service Voirie") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onServiceAssigned(serviceInput) },
            enabled = serviceInput != currentService.orEmpty()
        ) {
            Text("Enregistrer")
        }
    }
}

@Composable
private fun StatusHistoryRow(entry: IncidentStatusHistoryEntity, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            IncidentStatusBadge(status = entry.status)
            entry.commentaire?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            text = dateTimeFormat.format(entry.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Carte de position en lecture seule (osmdroid/OpenStreetMap) : centrée sur le
// signalement avec un marqueur, mise à jour si les coordonnées changent
// (nouvelle collecte du Flow), et libérée à la sortie de la composition.
@Composable
private fun IncidentLocationMap(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    // osmdroid instancie une vraie MapView Android (tuiles réseau, configuration
    // globale) : incompatible avec le rendu d'aperçu Compose Studio, on y
    // affiche donc un simple espace réservé plutôt que de planter la preview.
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "Carte", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val context = LocalContext.current
    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
        }
    }

    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            val point = GeoPoint(latitude, longitude)
            view.controller.setCenter(point)
            view.overlays.clear()
            view.overlays.add(
                Marker(view).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
            )
            view.invalidate()
        }
    )
}

@Preview(name = "Détail signalement admin", showBackground = true)
@Composable
private fun AdminIncidentDetailScreenPreview() {
    CitoyenProTheme {
        AdminIncidentDetailScreen(
            uiState = AdminIncidentDetailUiState(
                isLoading = false,
                incident = IncidentEntity(
                    id = 1,
                    titre = "Lampadaire cassé",
                    description = "Le lampadaire au coin de la rue est hors service depuis une semaine.",
                    categoryId = 1,
                    priority = Priority.HAUTE,
                    status = IncidentStatus.EN_COURS,
                    latitude = 33.5731,
                    longitude = -7.5898,
                    adresse = "Boulevard Mohammed V, Casablanca",
                    citoyenId = 1,
                    serviceAffecte = "Service Éclairage public",
                    dateCreation = System.currentTimeMillis(),
                    dateMaj = System.currentTimeMillis()
                ),
                categoryNom = "Éclairage public",
                history = listOf(
                    IncidentStatusHistoryEntity(
                        id = 1,
                        incidentId = 1,
                        status = IncidentStatus.RECU,
                        date = System.currentTimeMillis() - 86_400_000
                    ),
                    IncidentStatusHistoryEntity(
                        id = 2,
                        incidentId = 1,
                        status = IncidentStatus.EN_COURS,
                        date = System.currentTimeMillis(),
                        commentaire = "Service affecté : Service Éclairage public"
                    )
                )
            ),
            onStatusSelected = {},
            onServiceAssigned = {},
            onNavigateBack = {}
        )
    }
}
