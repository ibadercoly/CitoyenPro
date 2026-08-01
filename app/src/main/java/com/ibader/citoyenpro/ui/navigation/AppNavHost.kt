package com.ibader.citoyenpro.ui.navigation

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.IncidentUpdateService
import com.ibader.citoyenpro.data.repository.IncidentVoteRepository
import com.ibader.citoyenpro.data.repository.LocationRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.ui.auth.LoginRoute
import com.ibader.citoyenpro.ui.auth.RegisterRoute
import kotlinx.coroutines.launch

// Graphe racine : bascule entre le parcours d'authentification et l'espace
// citoyen/admin en fonction du rôle de l'utilisateur connecté. La navigation
// suit l'état de session (userRepository.currentUser) plutôt que les callbacks
// de succès des écrans, pour rester cohérente même si la session change
// ailleurs (ex. déconnexion depuis l'espace citoyen/admin).
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavHost(
    userRepository: UserRepository,
    incidentRepository: IncidentRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    incidentVoteRepository: IncidentVoteRepository,
    incidentUpdateService: IncidentUpdateService,
    categoryRepository: CategoryRepository,
    locationRepository: LocationRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by userRepository.currentUser.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val onLogout: () -> Unit = { coroutineScope.launch { userRepository.logout() } }

    // Tentative de rattrapage immédiate au lancement de l'app : rejoue la
    // file d'opérations laissées en attente par une session précédente
    // hors-ligne et rafraîchit Room avec les dernières données distantes, si
    // le réseau est disponible. Les tentatives suivantes (retour réseau en
    // cours de session, filet de sécurité périodique) sont prises en charge
    // par ConnectivitySyncTrigger + IncidentSyncWorker (cf. MainActivity).
    LaunchedEffect(Unit) {
        // Firebase persiste sa propre session sur disque : on la restaure ici
        // pour retrouver le profil applicatif (rôle) après un redémarrage du
        // process, avant de tenter de rejouer les opérations en attente.
        userRepository.restoreSession()
        incidentRepository.syncPendingChanges()
    }

    // Demandée dès qu'une session est ouverte (citoyen ou admin) : les
    // notifications de changement de statut peuvent être déclenchées depuis
    // l'un ou l'autre espace tant qu'aucun backend ne cible directement le
    // terminal du citoyen concerné (cf. IncidentStatusNotifier).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(currentUser, notificationPermissionState.status) {
            if (currentUser != null && !notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(currentUser) {
        val destination = when (currentUser?.role) {
            UserRole.CITOYEN -> AppRoute.CITIZEN_SPACE
            UserRole.ADMIN -> AppRoute.ADMIN_SPACE
            null -> AppRoute.LOGIN
        }
        if (navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.LOGIN,
        modifier = modifier
    ) {
        composable(AppRoute.LOGIN) {
            LoginRoute(
                userRepository = userRepository,
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(AppRoute.REGISTER) }
            )
        }
        composable(AppRoute.REGISTER) {
            RegisterRoute(
                userRepository = userRepository,
                onRegisterSuccess = {},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(AppRoute.CITIZEN_SPACE) {
            CitizenNavHost(
                onLogout = onLogout,
                incidentRepository = incidentRepository,
                incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                incidentVoteRepository = incidentVoteRepository,
                categoryRepository = categoryRepository,
                userRepository = userRepository,
                locationRepository = locationRepository
            )
        }
        composable(AppRoute.ADMIN_SPACE) {
            AdminNavHost(
                onLogout = onLogout,
                incidentRepository = incidentRepository,
                incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                categoryRepository = categoryRepository,
                incidentUpdateService = incidentUpdateService,
                userRepository = userRepository
            )
        }
    }
}
