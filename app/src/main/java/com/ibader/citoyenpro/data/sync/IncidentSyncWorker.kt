package com.ibader.citoyenpro.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.ibader.citoyenpro.data.local.database.AppDatabase
import com.ibader.citoyenpro.data.remote.RetrofitClient
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.util.NetworkMonitor
import java.util.concurrent.TimeUnit

// Pas de Hilt dans ce projet : comme MainActivity, ce worker construit son
// IncidentRepository à partir des mêmes singletons manuels (AppDatabase,
// RetrofitClient) plutôt que via une factory injectée — WorkManager peut
// l'instancier lui-même par réflexion (constructeur (Context, WorkerParameters)
// standard), aucun WorkerFactory personnalisé n'est donc nécessaire.
class IncidentSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = IncidentRepository(
            incidentDao = database.incidentDao(),
            pendingOperationDao = database.pendingIncidentOperationDao(),
            historyDao = database.incidentStatusHistoryDao(),
            voteDao = database.incidentVoteDao(),
            apiService = RetrofitClient.getApiService(),
            networkMonitor = NetworkMonitor(applicationContext),
            context = applicationContext
        )
        return if (repository.syncPendingChanges()) Result.success() else Result.retry()
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "incident_sync_periodic"
        private const val ONE_TIME_WORK_NAME = "incident_sync_one_time"

        private val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Filet de sécurité en tâche de fond, indépendant du déclenchement
        // immédiat par ConnectivitySyncTrigger : couvre le cas où l'app est
        // relancée par le système alors qu'elle était fermée pendant que le
        // réseau est revenu. KEEP : un enqueue à chaque lancement de l'app
        // (cf. MainActivity) ne doit pas réinitialiser une planification déjà
        // en place.
        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<IncidentSyncWorker>(
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            )
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        // Déclenché par ConnectivitySyncTrigger dès que le réseau redevient
        // disponible. REPLACE : si plusieurs coupures/retours se succèdent
        // rapidement, seule la tentative la plus récente compte (les
        // précédentes n'ont plus d'objet, syncPendingChanges() traite de
        // toute façon l'état actuel complet de la file).
        fun enqueueOneTime(context: Context) {
            val request = OneTimeWorkRequestBuilder<IncidentSyncWorker>()
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
