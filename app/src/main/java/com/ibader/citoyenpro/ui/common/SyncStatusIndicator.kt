package com.ibader.citoyenpro.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.sync.SyncState
import com.ibader.citoyenpro.data.sync.SyncStatusHolder

// Placé dans les TopAppBar citoyen/admin (cf. CitizenNavHost/AdminNavHost).
// Combine SyncStatusHolder (résultat de la dernière tentative, mis à jour
// par IncidentRepository.syncPendingChanges — appelée au lancement, au retour
// réseau et périodiquement, cf. MainActivity) et le nombre d'opérations
// encore en file (observé en direct depuis Room, donc à jour même si aucune
// synchro n'a encore été retentée depuis la dernière écriture hors-ligne).
@Composable
fun SyncStatusIndicator(
    incidentRepository: IncidentRepository,
    modifier: Modifier = Modifier
) {
    val syncState by SyncStatusHolder.state.collectAsStateWithLifecycle()
    val pendingCount by incidentRepository.pendingOperationCount.collectAsStateWithLifecycle(initialValue = 0)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        when {
            syncState == SyncState.SYNCING -> {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
            syncState == SyncState.OFFLINE || syncState == SyncState.ERROR -> {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = if (syncState == SyncState.OFFLINE) "Hors ligne" else "Échec de synchronisation",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            pendingCount > 0 -> {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Synchronisation en attente"
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Synchronisé",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (pendingCount > 0) {
            Spacer(Modifier.width(4.dp))
            Text(text = "$pendingCount", style = MaterialTheme.typography.labelSmall)
        }
    }
}
