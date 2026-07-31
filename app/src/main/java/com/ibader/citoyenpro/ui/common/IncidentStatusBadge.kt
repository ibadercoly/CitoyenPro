package com.ibader.citoyenpro.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ibader.citoyenpro.domain.model.IncidentStatus

// Couleur associée à chaque statut, cohérente avec la palette "confiance
// civique" : bleu (reçu) → ambre (en cours) → vert (résolu), gris neutre
// (clos) pour un signalement archivé sans action en cours.
@Composable
private fun IncidentStatus.containerColor(): Color = when (this) {
    IncidentStatus.RECU -> MaterialTheme.colorScheme.primaryContainer
    IncidentStatus.EN_COURS -> MaterialTheme.colorScheme.tertiaryContainer
    IncidentStatus.RESOLU -> MaterialTheme.colorScheme.secondaryContainer
    IncidentStatus.CLOS -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun IncidentStatus.contentColor(): Color = when (this) {
    IncidentStatus.RECU -> MaterialTheme.colorScheme.onPrimaryContainer
    IncidentStatus.EN_COURS -> MaterialTheme.colorScheme.onTertiaryContainer
    IncidentStatus.RESOLU -> MaterialTheme.colorScheme.onSecondaryContainer
    IncidentStatus.CLOS -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun IncidentStatusBadge(status: IncidentStatus, modifier: Modifier = Modifier) {
    Surface(
        color = status.containerColor(),
        contentColor = status.contentColor(),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = status.libelle,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
