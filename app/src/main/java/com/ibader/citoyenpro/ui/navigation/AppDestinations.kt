package com.ibader.citoyenpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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

// Onglets de la barre de navigation basse de l'espace admin. Avec 5 onglets,
// des libellés longs ("Tableau de bord", "Statistiques", "Utilisateurs")
// passent à la ligne ou débordent sur un écran de téléphone standard
// (~360-420dp) : les libellés restent donc volontairement courts (une seule
// ligne, sans troncature) plutôt que descriptifs, cf. AdminNavHost pour le
// style de texte associé.
sealed class AdminDestination(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : AdminDestination("admin_dashboard", "Accueil", Icons.Filled.Home)
    object Incidents : AdminDestination("admin_incidents", "Alertes", Icons.Filled.Warning)
    object Categories : AdminDestination("admin_categories", "Types", Icons.AutoMirrored.Filled.List)
    object Stats : AdminDestination("admin_stats", "Stats", Icons.Filled.Info)
    object Users : AdminDestination("admin_users", "Comptes", Icons.Filled.Person)

    companion object {
        val items = listOf(Dashboard, Incidents, Categories, Stats, Users)
    }
}
