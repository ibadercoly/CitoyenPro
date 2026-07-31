package com.ibader.citoyenpro.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Aperçu du système de design : palette de couleurs et échelle typographique,
// utile pour valider visuellement le thème en clair et en sombre.
@Composable
private fun ColorSwatch(name: String, background: Color, content: Color, modifier: Modifier = Modifier) {
    Surface(
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(64.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = name, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun ThemeShowcase(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "CitoyenPro", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Confiance civique", style = MaterialTheme.typography.bodyLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ColorSwatch(
                    "Primary",
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                    Modifier.weight(1f)
                )
                ColorSwatch(
                    "Secondary",
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                    Modifier.weight(1f)
                )
                ColorSwatch(
                    "Tertiary",
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary,
                    Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ColorSwatch(
                    "Primary Container",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    Modifier.weight(1f)
                )
                ColorSwatch(
                    "Secondary Container",
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                    Modifier.weight(1f)
                )
                ColorSwatch(
                    "Tertiary Container",
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer,
                    Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ColorSwatch(
                    "Error",
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.onError,
                    Modifier.weight(1f)
                )
                ColorSwatch(
                    "Surface Variant",
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    Modifier.weight(1f)
                )
            }

            Text(text = "Headline Medium", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Title Large", style = MaterialTheme.typography.titleLarge)
            Text(text = "Body Large — texte courant de l'application", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Label Medium", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview(name = "Thème clair", showBackground = true)
@Composable
private fun ThemeShowcaseLightPreview() {
    CitoyenProTheme(darkTheme = false) {
        ThemeShowcase()
    }
}

@Preview(name = "Thème sombre", showBackground = true)
@Composable
private fun ThemeShowcaseDarkPreview() {
    CitoyenProTheme(darkTheme = true) {
        ThemeShowcase()
    }
}
