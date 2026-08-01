package com.ibader.citoyenpro.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Palette catégorielle (statut, catégorie) : 8 teintes distinctes en
// séquence fixe (jamais réassignées par tri/filtre), variantes claire et
// sombre. Rampe séquentielle bleue (priorité) : claire → foncée porte la
// sévérité croissante, indépendamment du thème.
private val CategoricalLight = listOf(
    Color(0xFF2A78D6), Color(0xFFEB6834), Color(0xFF1BAF7A), Color(0xFFEDA100),
    Color(0xFFE87BA4), Color(0xFF008300), Color(0xFF4A3AA7), Color(0xFFE34948)
)
private val CategoricalDark = listOf(
    Color(0xFF3987E5), Color(0xFFD95926), Color(0xFF199E70), Color(0xFFC98500),
    Color(0xFFD55181), Color(0xFF008300), Color(0xFF9085E9), Color(0xFFE66767)
)
private val SequentialBlue = listOf(
    Color(0xFF86B6EF), Color(0xFF3987E5), Color(0xFF256ABF), Color(0xFF104281)
)

@Composable
private fun categoricalColors(): List<Color> = if (isSystemInDarkTheme()) CategoricalDark else CategoricalLight

// Composable "route" : instancie l'AdminStatsViewModel et lui délègue les
// agrégations réactives (statut / catégorie / priorité).
@Composable
fun AdminStatsRoute(
    incidentRepository: IncidentRepository,
    categoryRepository: CategoryRepository,
    modifier: Modifier = Modifier,
    viewModel: AdminStatsViewModel = viewModel(
        factory = AdminStatsViewModel.factory(incidentRepository, categoryRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminStatsScreen(uiState = uiState, modifier = modifier)
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun AdminStatsScreen(uiState: AdminStatsUiState, modifier: Modifier = Modifier) {
    val categorical = categoricalColors()
    val neutral = MaterialTheme.colorScheme.outline

    fun colorFor(entry: StatEntry, ramp: List<Color> = categorical): Color =
        if (entry.colorSlot < 0) neutral else ramp[entry.colorSlot % ramp.size]

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

        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Statistiques", style = MaterialTheme.typography.headlineSmall)

            CounterRow(uiState = uiState)

            HorizontalDivider()
            StatsSection(title = "Par statut") {
                PieWithLegend(entries = uiState.byStatus, colorFor = { colorFor(it) })
            }

            HorizontalDivider()
            StatsSection(title = "Par catégorie") {
                HorizontalBarList(entries = uiState.byCategory, colorFor = { colorFor(it) })
            }

            HorizontalDivider()
            StatsSection(title = "Par priorité") {
                HorizontalBarList(entries = uiState.byPriority, colorFor = { colorFor(it, SequentialBlue) })
            }
        }
    }
}

@Composable
private fun StatsSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

// Compteurs : total et un par statut, en cartes compactes — la vue
// d'ensemble avant le détail des graphiques.
@Composable
private fun CounterRow(uiState: AdminStatsUiState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CounterCard(label = "Total", value = uiState.totalIncidents, modifier = Modifier.weight(1f))
        uiState.byStatus.forEach { entry ->
            CounterCard(label = entry.label, value = entry.count, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CounterCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Camembert (Canvas, sans bibliothèque externe) + légende. Les tranches à
// zéro n'occupent pas d'espace dans le dessin mais restent dans la légende,
// pour un comptage complet malgré un cercle vide de cette part.
@Composable
private fun PieWithLegend(
    entries: List<StatEntry>,
    colorFor: (StatEntry) -> Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PieChart(
            entries = entries,
            colorFor = colorFor,
            modifier = Modifier.size(140.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        ChartLegend(entries = entries, colorFor = colorFor, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PieChart(entries: List<StatEntry>, colorFor: (StatEntry) -> Color, modifier: Modifier = Modifier) {
    val total = entries.sumOf { it.count }
    val gapDegrees = 2f

    Canvas(modifier = modifier) {
        if (total <= 0) return@Canvas
        val diameter = minOf(size.width, size.height)
        val topLeft = androidx.compose.ui.geometry.Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        entries.forEach { entry ->
            if (entry.count > 0) {
                val sweep = (entry.count.toFloat() / total) * 360f
                val drawnSweep = (sweep - gapDegrees).coerceAtLeast(1f)
                drawArc(
                    color = colorFor(entry),
                    startAngle = startAngle,
                    sweepAngle = drawnSweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun ChartLegend(
    entries: List<StatEntry>,
    colorFor: (StatEntry) -> Color,
    modifier: Modifier = Modifier
) {
    val total = entries.sumOf { it.count }.coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        entries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorFor(entry))
                )
                val percent = (entry.count * 100) / total
                Text(
                    text = "${entry.label} · ${entry.count} ($percent %)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Barres horizontales (Canvas via Box proportionnel) : privilégiées sur le
// camembert pour catégorie/priorité, où l'ordre et la comparaison de
// grandeur comptent plus que la composition d'un tout.
@Composable
private fun HorizontalBarList(
    entries: List<StatEntry>,
    colorFor: (StatEntry) -> Color,
    modifier: Modifier = Modifier
) {
    val maxCount = entries.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEach { entry ->
            HorizontalBarRow(entry = entry, color = colorFor(entry), maxCount = maxCount)
        }
    }
}

@Composable
private fun HorizontalBarRow(entry: StatEntry, color: Color, maxCount: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = entry.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = entry.count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val fraction = entry.count.toFloat() / maxCount.toFloat()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(color)
            )
        }
    }
}

@Preview(name = "Statistiques admin", showBackground = true)
@Composable
private fun AdminStatsScreenPreview() {
    CitoyenProTheme {
        AdminStatsScreen(
            uiState = AdminStatsUiState(
                isLoading = false,
                totalIncidents = 24,
                byStatus = listOf(
                    StatEntry("Reçu", 6, 0),
                    StatEntry("En cours", 9, 1),
                    StatEntry("Résolu", 7, 2),
                    StatEntry("Clos", 2, 3)
                ),
                byCategory = listOf(
                    StatEntry("Voirie", 10, 0),
                    StatEntry("Éclairage public", 6, 1),
                    StatEntry("Propreté", 5, 2),
                    StatEntry("Autres", 3, -1)
                ),
                byPriority = listOf(
                    StatEntry("Basse", 5, 0),
                    StatEntry("Moyenne", 10, 1),
                    StatEntry("Haute", 7, 2),
                    StatEntry("Urgente", 2, 3)
                )
            )
        )
    }
}
