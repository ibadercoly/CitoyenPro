package com.ibader.citoyenpro.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ibader.citoyenpro.R
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Logo partagé par les écrans d'authentification (connexion, inscription),
// affiché au-dessus du formulaire. Centré dans un Box pleine largeur plutôt
// que via un align() côté appelant : reste centré quel que soit le parent
// (Column ou non), sans faire porter cette responsabilité à chaque écran.
// Largeur fixe (ni fillMaxWidth ni dépendante du contenu) pour garder une
// taille raisonnable même sur un grand écran.
@Composable
fun AuthLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.citoyenpro_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(140.dp)
        )
    }
}

@Preview(name = "Logo auth", showBackground = true)
@Composable
private fun AuthLogoPreview() {
    CitoyenProTheme {
        AuthLogo()
    }
}
