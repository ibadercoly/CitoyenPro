package com.ibader.citoyenpro.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ibader.citoyenpro.ui.theme.AppCardCornerRadius
import com.ibader.citoyenpro.ui.theme.AppControlCornerRadius

// Langage visuel partagé par TOUTE l'app (espace citoyen ET admin, écrans
// d'authentification inclus) : fond en dégradé, cartes sans ombre portée aux
// coins arrondis, champs et bouton principal cohérents. Centralisé ici plutôt
// que recopié écran par écran : changer une couleur ou un rayon se fait dans
// ce fichier (ou dans ui.theme.Shape/Color/Theme) et se répercute partout.

// Fond partagé de tous les écrans : dégradé subtil primaryContainer ->
// background, plus un arc décoratif discret en haut. Utilisé aussi bien par
// les écrans d'authentification que par les onglets citoyen/admin.
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 140.dp, bottomEnd = 140.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                        )
                    )
                )
        )
        content()
    }
}

// Barre supérieure partagée : transparente (laisse voir le dégradé
// AppBackground derrière elle, comme le fond continu des écrans d'auth) et
// titre en bleu primary gras — même hiérarchie que les titres d'écran
// (headlineSmall/Medium) sans recolorer titleLarge globalement, ce qui
// affecterait aussi les titres de cartes/listes qui doivent rester neutres.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
    )
}

// Carte de contenu partagée : coins arrondis, sans ombre portée, bordure fine
// plutôt qu'un fond gris plat élevé par tonalElevation. Remplace les Card(...)
// recopiées dans chaque écran (accueil, listes, détails, admin...).
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppCardCornerRadius)
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    if (onClick != null) {
        Card(onClick = onClick, shape = shape, colors = colors, elevation = elevation, border = border, modifier = modifier) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(contentPadding),
                verticalArrangement = verticalArrangement,
                content = content
            )
        }
    } else {
        Card(shape = shape, colors = colors, elevation = elevation, border = border, modifier = modifier) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(contentPadding),
                verticalArrangement = verticalArrangement,
                content = content
            )
        }
    }
}

// Forme et couleurs partagées par tous les champs de saisie (OutlinedTextField)
// de l'app, y compris ceux qui ne peuvent pas passer par AppTextField
// directement (menu déroulant, champs de formulaire dans une AlertDialog...).
// Un seul endroit à modifier pour changer l'arrondi ou la couleur de focus de
// tous les champs.
val AppTextFieldShape = RoundedCornerShape(AppControlCornerRadius)

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

// Champ de saisie stylisé complet (icône de tête optionnelle, coins arrondis,
// couleurs de focus du thème) pour les cas standards à une ligne.
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorText: String? = null,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { text -> { Text(text) } },
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(imageVector = icon, contentDescription = null) }
        },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        supportingText = errorText?.let { text -> { Text(text) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        enabled = enabled,
        shape = AppTextFieldShape,
        colors = appTextFieldColors(),
        modifier = modifier.fillMaxWidth()
    )
}

// Bouton d'action principal partagé (connexion, inscription, envoi d'un
// signalement...) : mêmes coins arrondis, hauteur et indicateur de
// chargement partout où l'app a besoin d'un CTA plein largeur.
@Composable
fun AppPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Retour haptique explicite au clic : sur un backend local (dev), un
    // envoi se termine en quelques millisecondes, trop vite pour que
    // l'indicateur de chargement soit visible à l'œil — sans cette
    // vibration, l'utilisateur n'a alors aucune confirmation perceptible
    // que le bouton a bien réagi à son appui.
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(AppControlCornerRadius),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text)
        }
    }
}

// Palette partagée par les barres de navigation basses (citoyen et admin) :
// item sélectionné en bleu primary sur pastille primaryContainer, item
// inactif en gris discret onSurfaceVariant.
@Composable
fun appNavigationBarColors(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)
