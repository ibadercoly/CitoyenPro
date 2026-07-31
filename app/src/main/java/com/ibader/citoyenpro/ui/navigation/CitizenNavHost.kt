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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ibader.citoyenpro.ui.citizen.CitizenHomeScreen
import com.ibader.citoyenpro.ui.citizen.CitizenProfileScreen
import com.ibader.citoyenpro.ui.citizen.CitizenReportsScreen

// Espace citoyen : barre de navigation basse (Accueil / Mes signalements / Profil)
// et bouton de déconnexion, au-dessus d'un NavHost imbriqué pour les onglets.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenNavHost(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Espace citoyen") },
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
            composable(CitizenDestination.Home.route) { CitizenHomeScreen() }
            composable(CitizenDestination.Reports.route) { CitizenReportsScreen() }
            composable(CitizenDestination.Profile.route) { CitizenProfileScreen() }
        }
    }
}
