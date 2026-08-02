package com.ibader.citoyenpro.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.ui.admin.AdminCategoriesRoute
import com.ibader.citoyenpro.ui.admin.AdminDashboardRoute
import com.ibader.citoyenpro.ui.admin.AdminIncidentDetailRoute
import com.ibader.citoyenpro.ui.admin.AdminIncidentsRoute
import com.ibader.citoyenpro.ui.admin.AdminStatsRoute
import com.ibader.citoyenpro.ui.admin.AdminUsersRoute
import com.ibader.citoyenpro.ui.common.AppTopBar
import com.ibader.citoyenpro.ui.common.SyncStatusIndicator
import com.ibader.citoyenpro.ui.common.appNavigationBarColors

private const val INCIDENT_DETAIL_ROUTE = "admin_incident_detail"
private const val INCIDENT_ID_ARG = "incidentId"

// Espace admin : barre de navigation basse (5 onglets, cf. AdminDestination)
// et bouton de déconnexion, au-dessus d'un NavHost imbriqué pour les onglets.
// Le détail d'un signalement est empilé par-dessus les onglets (pas un
// onglet lui-même), comme côté citoyen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavHost(
    onLogout: () -> Unit,
    incidentRepository: IncidentRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    categoryRepository: CategoryRepository,
    incidentUpdateService: IncidentUpdateService,
    userRepository: UserRepository,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        topBar = {
            AppTopBar(
                title = "Espace administrateur",
                actions = {
                    SyncStatusIndicator(
                        incidentRepository = incidentRepository,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    TextButton(onClick = onLogout) {
                        Text("Déconnexion")
                    }
                }
            )
        },
        bottomBar = {
            val backStackEntry by tabNavController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
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
                        colors = appNavigationBarColors(),
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        // labelSmall (plutôt que labelMedium, la taille par défaut de
                        // NavigationBarItem) + une seule ligne forcée : à 5 onglets sur
                        // un écran de téléphone standard (~360-420dp), la taille par
                        // défaut fait passer les libellés les plus longs à la ligne, ce
                        // qui casse aussi le centrage vertical icône/texte. Le libellé
                        // est déjà volontairement court (cf. AppDestinations) ; l'ellipse
                        // n'est qu'un filet de sécurité (réglages d'accessibilité en
                        // grande police, etc.), pas le comportement attendu en pratique.
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
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
            composable(AdminDestination.Dashboard.route) {
                AdminDashboardRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    onIncidentClick = { incidentId ->
                        tabNavController.navigate("$INCIDENT_DETAIL_ROUTE/$incidentId")
                    }
                )
            }
            composable(AdminDestination.Incidents.route) {
                AdminIncidentsRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    onIncidentClick = { incidentId ->
                        tabNavController.navigate("$INCIDENT_DETAIL_ROUTE/$incidentId")
                    }
                )
            }
            composable(AdminDestination.Categories.route) {
                AdminCategoriesRoute(categoryRepository = categoryRepository)
            }
            composable(AdminDestination.Stats.route) {
                AdminStatsRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository
                )
            }
            composable(AdminDestination.Users.route) { AdminUsersRoute(userRepository = userRepository) }
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
