package com.ibader.citoyenpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.ui.auth.LoginRoute
import com.ibader.citoyenpro.ui.auth.RegisterRoute

// Graphe racine : bascule entre le parcours d'authentification et l'espace
// citoyen/admin en fonction du rôle de l'utilisateur connecté. La navigation
// suit l'état de session (userRepository.currentUser) plutôt que les callbacks
// de succès des écrans, pour rester cohérente même si la session change
// ailleurs (ex. déconnexion depuis l'espace citoyen/admin).
@Composable
fun AppNavHost(
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by userRepository.currentUser.collectAsStateWithLifecycle()

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
            CitizenNavHost(onLogout = userRepository::logout)
        }
        composable(AppRoute.ADMIN_SPACE) {
            AdminNavHost(onLogout = userRepository::logout)
        }
    }
}
