package com.ibader.citoyenpro.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ibader.citoyenpro.MainActivity
import com.ibader.citoyenpro.R

// Reçoit les push envoyées au topic "user_<firebaseUid>" du citoyen concerné
// (abonnement géré par UserRepository lors de la connexion/inscription) pour le
// notifier d'une mise à jour de signalement même quand l'app tourne sur un
// autre appareil que celui où le changement a été fait — ce qu'IncidentStatusNotifier
// seul ne permet pas, faute de backend capable de cibler un terminal précis.
class FcmService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        // Garantit que le canal existe même si ce process n'a jamais lancé
        // MainActivity (démarrage à froid déclenché par la réception du push).
        IncidentStatusNotifier.ensureChannel(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Messages envoyés sans bloc "notification" (data-only) : à afficher nous-
        // mêmes. Les messages avec bloc "notification" reçus app en arrière-plan
        // sont déjà affichés directement par le système (cf. meta-data dans
        // AndroidManifest.xml) et ne remontent pas ici.
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        val incidentId = message.data["incidentId"]?.toIntOrNull() ?: 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            incidentId,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, IncidentStatusNotifier.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(incidentId, notification)
    }
}
