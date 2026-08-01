package com.ibader.citoyenpro.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR, OFFLINE }

// État de synchro partagé entre IncidentRepository (qui le met à jour, que
// l'appel vienne de l'UI au lancement ou d'IncidentSyncWorker en arrière-plan)
// et l'indicateur affiché dans les barres supérieures citoyen/admin. Un
// singleton process (comme AppDatabase/RetrofitClient) plutôt qu'un champ du
// repository : IncidentSyncWorker construit sa propre instance de
// IncidentRepository (pas de DI), l'état doit donc vivre en dehors d'elle
// pour rester visible de l'UI.
object SyncStatusHolder {
    private val _state = MutableStateFlow(SyncState.IDLE)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    fun onSyncStarted() {
        _state.value = SyncState.SYNCING
    }

    fun onSyncSucceeded() {
        _state.value = SyncState.SUCCESS
    }

    fun onSyncFailed() {
        _state.value = SyncState.ERROR
    }

    fun onOffline() {
        _state.value = SyncState.OFFLINE
    }
}
