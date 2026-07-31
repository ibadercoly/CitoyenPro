package com.ibader.citoyenpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ibader.citoyenpro.data.local.database.AppDatabase
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.IncidentUpdateService
import com.ibader.citoyenpro.data.repository.LocationRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.notification.IncidentStatusNotifier
import com.ibader.citoyenpro.ui.navigation.AppNavHost
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme
import java.io.File
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Config osmdroid (carte de position des signalements) : identifiant
        // d'agent requis par la politique d'utilisation des tuiles OpenStreetMap,
        // et cache de tuiles dans le stockage privé de l'app (pas besoin de
        // permission de stockage externe).
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(applicationContext.filesDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }

        val database = AppDatabase.getInstance(applicationContext)
        val userRepository = UserRepository(database.userDao())
        val incidentRepository = IncidentRepository(database.incidentDao())
        val incidentStatusHistoryRepository = IncidentStatusHistoryRepository(database.incidentStatusHistoryDao())
        val categoryRepository = CategoryRepository(database.categoryDao())
        val locationRepository = LocationRepository(applicationContext)
        val incidentStatusNotifier = IncidentStatusNotifier(applicationContext)
        val incidentUpdateService = IncidentUpdateService(
            incidentRepository,
            incidentStatusHistoryRepository,
            incidentStatusNotifier
        )

        setContent {
            CitoyenProTheme {
                AppNavHost(
                    userRepository = userRepository,
                    incidentRepository = incidentRepository,
                    incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                    incidentUpdateService = incidentUpdateService,
                    categoryRepository = categoryRepository,
                    locationRepository = locationRepository
                )
            }
        }
    }
}
