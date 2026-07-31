package com.ibader.citoyenpro.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class GeoPosition(
    val latitude: Double,
    val longitude: Double,
    val adresse: String?
)

// S'appuie sur FusedLocationProviderClient (Google Play Services) pour la
// position GPS et sur Geocoder pour le géocodage inverse (résolution en
// adresse lisible, sur la base d'un meilleur effort : peut échouer selon la
// connectivité ou la disponibilité du service sur l'appareil). L'appelant est
// responsable d'avoir déjà obtenu la permission de localisation
// (ACCESS_FINE_LOCATION ou ACCESS_COARSE_LOCATION).
class LocationRepository(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentPosition(): GeoPosition? {
        val location = suspendCancellableCoroutine<Location?> { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            )
                .addOnSuccessListener { location -> if (continuation.isActive) continuation.resume(location) }
                .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
        } ?: return null

        return GeoPosition(
            latitude = location.latitude,
            longitude = location.longitude,
            adresse = reverseGeocode(location.latitude, location.longitude)
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                // Surcharge synchrone dépréciée au profit d'un callback asynchrone
                // (API 33+), mais toujours fonctionnelle sur toutes les versions
                // supportées (minSdk 24) ; on est déjà hors thread principal ici.
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
            }.getOrNull()
        }
}
