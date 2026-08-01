package com.ibader.citoyenpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

// Routes du graphe racine : bascule entre le parcours non connecté (auth)
// et les espaces "citoyen" / "admin" une fois la session ouverte.
object AppRoute {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CITIZEN_SPACE = "citizen_space"
    const val ADMIN_SPACE = "admin_space"
}

// Onglets de la barre de navigation basse de l'espace citoyen.
sealed class CitizenDestination(val route: String, val label: String, val icon: ImageVector) {
    object Home : CitizenDestination("citizen_home", "Accueil", Icons.Filled.Home)
    object Reports : CitizenDestination("citizen_reports", "Signalements", Icons.AutoMirrored.Filled.List)
    object Community : CitizenDestination("citizen_community", "Communauté", Icons.Filled.ThumbUp)
    object Profile : CitizenDestination("citizen_profile", "Profil", Icons.Filled.Person)

    companion object {
        val items = listOf(Home, Reports, Community, Profile)
    }
}

// Onglets de la barre de navigation basse de l'espace admin.
sealed class AdminDestination(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : AdminDestination("admin_dashboard", "Tableau de bord", Icons.Filled.Home)
    object Incidents : AdminDestination("admin_incidents", "Signalements", Icons.Filled.Warning)
    object Users : AdminDestination("admin_users", "Utilisateurs", Icons.Filled.Person)

    companion object {
        val items = listOf(Dashboard, Incidents, Users)
    }
}
