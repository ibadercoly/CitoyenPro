package com.ibader.citoyenpro.ui.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.Badge
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.domain.model.badgesUnlocked
import com.ibader.citoyenpro.domain.model.libelle
import com.ibader.citoyenpro.domain.model.nextBadge
import com.ibader.citoyenpro.ui.common.AppBackground
import com.ibader.citoyenpro.ui.common.AppCard
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Composable "route" : instancie le CitizenProfileViewModel et lui délègue
// les informations du citoyen connecté (session + Flow Room pour le compteur).
@Composable
fun CitizenProfileRoute(
    incidentRepository: IncidentRepository,
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    viewModel: CitizenProfileViewModel = viewModel(
        factory = CitizenProfileViewModel.factory(incidentRepository, userRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CitizenProfileScreen(uiState = uiState, modifier = modifier)
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
// Même langage graphique que les écrans d'authentification (fond dégradé
// AppBackground, cartes sans ombre aux coins arrondis) pour une cohérence
// visuelle sur toute l'app plutôt qu'un fond blanc plat et un bloc gris isolé.
@Composable
fun CitizenProfileScreen(uiState: CitizenProfileUiState, modifier: Modifier = Modifier) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = uiState.nom,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            RolePill(role = uiState.role)
            Spacer(Modifier.height(28.dp))

            AppCard(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileInfoRow(icon = Icons.Filled.Person, label = "Nom", value = uiState.nom)
                ProfileInfoRow(icon = Icons.Filled.Email, label = "Email", value = uiState.email)
                ProfileInfoRow(icon = Icons.Filled.Shield, label = "Rôle", value = uiState.role.libelle())
                ProfileInfoRow(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    label = "Signalements envoyés",
                    value = uiState.totalIncidents.toString()
                )
            }
            Spacer(Modifier.height(16.dp))

            CitizenPointsCard(points = uiState.points)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Pastille de rôle colorée (plutôt qu'un simple texte) pour mettre le rôle en
// valeur juste sous le nom, comme un statut.
@Composable
private fun RolePill(role: UserRole, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(imageVector = Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = role.libelle(), style = MaterialTheme.typography.labelMedium)
        }
    }
}

// Carte "points citoyens" : donne du contenu utile à l'écran (au lieu d'un
// grand vide sous la carte d'identité) en s'appuyant sur le système de
// points/badges déjà existant (cf. domain.model.Badge).
@Composable
private fun CitizenPointsCard(points: Int, modifier: Modifier = Modifier) {
    val unlocked = badgesUnlocked(points)
    val current: Badge? = unlocked.maxByOrNull { it.seuil }
    val next = nextBadge(points)

    AppCard(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "$points points citoyens",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = current?.let { "Palier actuel : ${it.libelle}" } ?: "Signalez pour gagner vos premiers points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        if (next != null) {
            val progress = if (next.seuil > 0) (points.toFloat() / next.seuil).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
            )
            Text(
                text = "Encore ${(next.seuil - points).coerceAtLeast(0)} points pour ${next.libelle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tous les badges débloqués, merci pour votre engagement !",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Preview(name = "Profil citoyen", showBackground = true)
@Composable
private fun CitizenProfileScreenPreview() {
    CitoyenProTheme {
        CitizenProfileScreen(
            uiState = CitizenProfileUiState(
                isLoading = false,
                nom = "Awa Ndiaye",
                email = "awa@example.com",
                role = UserRole.CITOYEN,
                totalIncidents = 3,
                points = 35
            )
        )
    }
}

@Preview(name = "Profil citoyen — tous badges", showBackground = true)
@Composable
private fun CitizenProfileScreenAllBadgesPreview() {
    CitoyenProTheme {
        CitizenProfileScreen(
            uiState = CitizenProfileUiState(
                isLoading = false,
                nom = "Moussa Diop",
                email = "moussa@example.com",
                role = UserRole.CITOYEN,
                totalIncidents = 42,
                points = 300
            )
        )
    }
}
