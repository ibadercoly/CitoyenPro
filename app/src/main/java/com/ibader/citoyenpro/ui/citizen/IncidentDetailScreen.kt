package com.ibader.citoyenpro.ui.citizen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

// Composable "route" : instancie l'IncidentDetailViewModel pour l'incident
// désigné par son id (passé en argument de navigation) et lui délègue le
// détail réactif (incident + catégorie + historique de statuts en Flow).
@Composable
fun IncidentDetailRoute(
    incidentId: Long,
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncidentDetailViewModel = viewModel(
        factory = IncidentDetailViewModel.factory(
            incidentId,
            incidentRepository,
            categoryRepository,
            incidentStatusHistoryRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    IncidentDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreen(
    uiState: IncidentDetailUiState,
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
                }

                Text(text = incident.description, style = MaterialTheme.typography.bodyLarge)

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
                    Text(text = "Historique des statuts", style = MaterialTheme.typography.titleMedium)
                    if (uiState.history.isEmpty()) {
                        Text(
                            text = "Aucun changement de statut pour l'instant.",
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

@Preview(name = "Détail signalement", showBackground = true)
@Composable
private fun IncidentDetailScreenPreview() {
    CitoyenProTheme {
        IncidentDetailScreen(
            uiState = IncidentDetailUiState(
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
                        date = System.currentTimeMillis()
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}
