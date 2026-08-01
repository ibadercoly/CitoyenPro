package com.ibader.citoyenpro.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ibader.citoyenpro.MainActivity
import com.ibader.citoyenpro.R
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.domain.model.IncidentStatus

// Notifications locales (déclenchées directement sur l'appareil courant, sans
// backend) informant qu'un signalement a été mis à jour (statut ou service
// affecté). Tant qu'aucun serveur ne relaie l'information au terminal du
// citoyen concerné, cette notification s'affiche sur l'appareil où l'action a
// eu lieu (ex. celui de l'admin) — Firebase Cloud Messaging la remplacera pour
// cibler réellement le terminal du citoyen.
class IncidentStatusNotifier(private val context: Context) {

    init {
        ensureChannel(context)
    }

    fun notifyStatusChanged(incident: IncidentEntity, newStatus: IncidentStatus) {
        notify(incident, "Nouveau statut : ${newStatus.libelle}")
    }

    fun notifyServiceAssigned(incident: IncidentEntity, service: String?) {
        val contentText = if (service.isNullOrBlank()) {
            "Le service affecté à votre signalement a été retiré"
        } else {
            "Votre signalement a été affecté au service : $service"
        }
        notify(incident, contentText)
    }

    private fun notify(incident: IncidentEntity, contentText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            incident.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(incident.titre)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(incident.id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "incident_status_updates"

        // Idempotent : appelable à la fois par IncidentStatusNotifier (déclenché
        // depuis l'appareil courant) et FcmService (push distante reçue alors que
        // MainActivity n'a jamais tourné dans ce process), pour garantir que le
        // canal existe avant de poster une notification.
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Suivi des signalements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifie le citoyen lorsqu'un de ses signalements est mis à jour"
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
