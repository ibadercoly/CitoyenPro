package com.ibader.citoyenpro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CivicBlue40,
    onPrimary = Color.White,
    primaryContainer = CivicBlue90,
    onPrimaryContainer = CivicBlue10,
    secondary = CivicGreen40,
    onSecondary = Color.White,
    secondaryContainer = CivicGreen90,
    onSecondaryContainer = CivicGreen10,
    tertiary = CivicAmber40,
    onTertiary = Color.White,
    tertiaryContainer = CivicAmber90,
    onTertiaryContainer = CivicAmber10,
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = CivicBlue80
)

private val DarkColorScheme = darkColorScheme(
    primary = CivicBlue80,
    onPrimary = CivicBlue20,
    primaryContainer = CivicBlue30,
    onPrimaryContainer = CivicBlue90,
    secondary = CivicGreen80,
    onSecondary = CivicGreen20,
    secondaryContainer = CivicGreen30,
    onSecondaryContainer = CivicGreen90,
    tertiary = CivicAmber80,
    onTertiary = CivicAmber20,
    tertiaryContainer = CivicAmber30,
    onTertiaryContainer = CivicAmber90,
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = CivicBlue40
)

@Composable
fun CitoyenProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Désactivée par défaut : la palette « confiance civique » est une identité de
    // marque volontaire, qu'on ne veut pas voir remplacée par le fond d'écran de l'utilisateur.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CitoyenProShapes,
        content = content
    )
}
