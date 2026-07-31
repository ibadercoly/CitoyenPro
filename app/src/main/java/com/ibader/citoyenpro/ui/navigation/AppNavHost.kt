package com.ibader.citoyenpro.ui.navigation

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ibader.citoyenpro.data.repository.LocationRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.ui.auth.LoginRoute
import com.ibader.citoyenpro.ui.auth.RegisterRoute

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
    incidentUpdateService: IncidentUpdateService,
    categoryRepository: CategoryRepository,
    locationRepository: LocationRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by userRepository.currentUser.collectAsStateWithLifecycle()

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
                onLogout = userRepository::logout,
                incidentRepository = incidentRepository,
                incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                categoryRepository = categoryRepository,
                userRepository = userRepository,
                locationRepository = locationRepository
            )
        }
        composable(AppRoute.ADMIN_SPACE) {
            AdminNavHost(
                onLogout = userRepository::logout,
                incidentRepository = incidentRepository,
                incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                categoryRepository = categoryRepository,
                incidentUpdateService = incidentUpdateService
            )
        }
    }
}
