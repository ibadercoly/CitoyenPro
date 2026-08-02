package com.ibader.citoyenpro.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Formes légèrement arrondies : sobre et professionnel, tout en restant accessible.
val CitoyenProShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Rayons partagés par les composants réutilisables (AppCard, AppTextField,
// AppPrimaryButton dans ui.common) : un seul endroit à modifier pour changer
// l'arrondi des cartes ou des champs/boutons sur toute l'app, plutôt que des
// valeurs magiques recopiées écran par écran.
val AppCardCornerRadius = 20.dp
val AppControlCornerRadius = 16.dp
