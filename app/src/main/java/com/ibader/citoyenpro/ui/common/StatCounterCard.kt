package com.ibader.citoyenpro.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Carte compacte "valeur + libellé" pour les compteurs de synthèse
// (AdminDashboardScreen, AdminStatsScreen, CitizenHomeScreen) — construite sur
// AppCard, donc alignée sur le même style (coins arrondis, sans ombre) que
// toutes les autres cartes de l'app en un seul endroit.
@Composable
fun StatCounterCard(label: String, value: Int, modifier: Modifier = Modifier) {
    AppCard(
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
