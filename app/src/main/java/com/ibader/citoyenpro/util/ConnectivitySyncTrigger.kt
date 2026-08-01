package com.ibader.citoyenpro.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.ibader.citoyenpro.data.sync.IncidentSyncWorker

// Contrairement à NetworkMonitor.isOnline() (lecture ponctuelle utilisée au
// moment d'une écriture/synchro), ceci écoute en direct les changements de
// connectivité via ConnectivityManager.NetworkCallback pour déclencher une
// synchronisation dès que le réseau redevient disponible — y compris quand
// l'app reste ouverte sans qu'aucune nouvelle écriture ne la redéclenche.
//
// Le projet n'a pas de classe Application ni de DI (cf. MainActivity) : ce
// singleton s'enregistre une seule fois par processus (garde `started`) même
// si start() est rappelé à chaque recréation de MainActivity (rotation...).
object ConnectivitySyncTrigger {

    @Volatile
    private var started = false

    fun start(context: Context) {
        if (started) return
        synchronized(this) {
            if (started) return
            registerCallback(context.applicationContext)
            started = true
        }
    }

    private fun registerCallback(appContext: Context) {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    IncidentSyncWorker.enqueueOneTime(appContext)
                }
            }
        )
    }
}
