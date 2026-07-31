package com.ibader.citoyenpro.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.IncidentUpdateService
import com.ibader.citoyenpro.ui.admin.AdminDashboardScreen
import com.ibader.citoyenpro.ui.admin.AdminIncidentDetailRoute
import com.ibader.citoyenpro.ui.admin.AdminIncidentsRoute
import com.ibader.citoyenpro.ui.admin.AdminUsersScreen

private const val INCIDENT_DETAIL_ROUTE = "admin_incident_detail"
private const val INCIDENT_ID_ARG = "incidentId"

// Espace admin : barre de navigation basse (Tableau de bord / Signalements /
// Utilisateurs) et bouton de déconnexion, au-dessus d'un NavHost imbriqué
// pour les onglets. Le détail d'un signalement est empilé par-dessus les
// onglets (pas un onglet lui-même), comme côté citoyen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavHost(
    onLogout: () -> Unit,
    incidentRepository: IncidentRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    categoryRepository: CategoryRepository,
    incidentUpdateService: IncidentUpdateService,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Espace administrateur") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Déconnexion")
                    }
                }
            )
        },
        bottomBar = {
            val backStackEntry by tabNavController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar {
                AdminDestination.items.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            tabNavController.navigate(destination.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = AdminDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminDestination.Dashboard.route) { AdminDashboardScreen() }
            composable(AdminDestination.Incidents.route) {
                AdminIncidentsRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    onIncidentClick = { incidentId ->
                        tabNavController.navigate("$INCIDENT_DETAIL_ROUTE/$incidentId")
                    }
                )
            }
            composable(AdminDestination.Users.route) { AdminUsersScreen() }
            composable(
                route = "$INCIDENT_DETAIL_ROUTE/{$INCIDENT_ID_ARG}",
                arguments = listOf(navArgument(INCIDENT_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getLong(INCIDENT_ID_ARG) ?: return@composable

                AdminIncidentDetailRoute(
                    incidentId = incidentId,
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                    incidentUpdateService = incidentUpdateService,
                    onNavigateBack = { tabNavController.popBackStack() }
                )
            }
        }
    }
}
