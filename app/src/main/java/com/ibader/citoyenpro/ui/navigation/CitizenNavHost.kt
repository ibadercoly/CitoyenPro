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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.ibader.citoyenpro.data.repository.IncidentVoteRepository
import com.ibader.citoyenpro.data.repository.LocationRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.ui.citizen.CitizenHomeRoute
import com.ibader.citoyenpro.ui.citizen.CitizenProfileRoute
import com.ibader.citoyenpro.ui.citizen.CreateIncidentRoute
import com.ibader.citoyenpro.ui.citizen.IncidentDetailRoute
import com.ibader.citoyenpro.ui.citizen.MyIncidentsRoute
import com.ibader.citoyenpro.ui.citizen.PublicIncidentsRoute
import com.ibader.citoyenpro.ui.common.SyncStatusIndicator

private const val CREATE_INCIDENT_ROUTE = "citizen_create_incident"
private const val INCIDENT_DETAIL_ROUTE = "citizen_incident_detail"
private const val INCIDENT_ID_ARG = "incidentId"
// Clé du SavedStateHandle de l'entrée "Mes signalements" utilisée pour lui
// signaler qu'un signalement vient d'être créé (résultat de navigation
// standard entre deux entrées du même NavHost).
private const val INCIDENT_CREATED_KEY = "incident_created"

// Espace citoyen : barre de navigation basse (Accueil / Mes signalements / Communauté / Profil)
// et bouton de déconnexion, au-dessus d'un NavHost imbriqué pour les onglets.
// L'écran de création de signalement est empilé par-dessus les onglets (pas
// un onglet lui-même) : il garde sa propre barre supérieure avec retour.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenNavHost(
    onLogout: () -> Unit,
    incidentRepository: IncidentRepository,
    incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    incidentVoteRepository: IncidentVoteRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    locationRepository: LocationRepository,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Espace citoyen") },
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

            NavigationBar {
                CitizenDestination.items.forEach { destination ->
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
            startDestination = CitizenDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CitizenDestination.Home.route) {
                CitizenHomeRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    userRepository = userRepository,
                    onCreateIncidentClick = { tabNavController.navigate(CREATE_INCIDENT_ROUTE) },
                    onIncidentClick = { incidentId ->
                        tabNavController.navigate("$INCIDENT_DETAIL_ROUTE/$incidentId")
                    }
                )
            }
            composable(CitizenDestination.Reports.route) { backStackEntry ->
                val incidentCreated by backStackEntry.savedStateHandle
                    .getStateFlow(INCIDENT_CREATED_KEY, false)
                    .collectAsStateWithLifecycle()

                MyIncidentsRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    userRepository = userRepository,
                    showConfirmation = incidentCreated,
                    onConfirmationShown = { backStackEntry.savedStateHandle[INCIDENT_CREATED_KEY] = false },
                    onCreateIncidentClick = { tabNavController.navigate(CREATE_INCIDENT_ROUTE) },
                    onIncidentClick = { incidentId ->
                        tabNavController.navigate("$INCIDENT_DETAIL_ROUTE/$incidentId")
                    }
                )
            }
            composable(CitizenDestination.Community.route) {
                PublicIncidentsRoute(
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    incidentVoteRepository = incidentVoteRepository,
                    userRepository = userRepository,
                    onIncidentClick = { incidentId ->
                        tabNavController.navigate("$INCIDENT_DETAIL_ROUTE/$incidentId")
                    }
                )
            }
            composable(CitizenDestination.Profile.route) {
                CitizenProfileRoute(
                    incidentRepository = incidentRepository,
                    userRepository = userRepository
                )
            }
            composable(CREATE_INCIDENT_ROUTE) {
                CreateIncidentRoute(
                    incidentRepository = incidentRepository,
                    incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                    categoryRepository = categoryRepository,
                    userRepository = userRepository,
                    locationRepository = locationRepository,
                    onIncidentCreated = {
                        tabNavController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(INCIDENT_CREATED_KEY, true)
                        tabNavController.popBackStack(CitizenDestination.Reports.route, inclusive = false)
                    },
                    onNavigateBack = { tabNavController.popBackStack() }
                )
            }
            composable(
                route = "$INCIDENT_DETAIL_ROUTE/{$INCIDENT_ID_ARG}",
                arguments = listOf(navArgument(INCIDENT_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getLong(INCIDENT_ID_ARG) ?: return@composable

                IncidentDetailRoute(
                    incidentId = incidentId,
                    incidentRepository = incidentRepository,
                    categoryRepository = categoryRepository,
                    incidentStatusHistoryRepository = incidentStatusHistoryRepository,
                    onNavigateBack = { tabNavController.popBackStack() }
                )
            }
        }
    }
}
