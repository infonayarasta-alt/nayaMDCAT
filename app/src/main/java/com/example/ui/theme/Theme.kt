package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityDarkPrimary,
    onPrimary = HighDensityDarkOnPrimary,
    primaryContainer = HighDensityDarkPrimaryContainer,
    onPrimaryContainer = HighDensityDarkOnPrimaryContainer,
    background = HighDensityDarkBackground,
    onBackground = HighDensityDarkOnBackground,
    surface = HighDensityDarkSurface,
    onSurface = HighDensityDarkOnSurface,
    surfaceVariant = HighDensityDarkSurfaceVariant,
    onSurfaceVariant = HighDensityDarkOnSurfaceVariant,
    outline = HighDensityDarkOutline,
    secondary = HighDensityDarkSecondary,
    secondaryContainer = HighDensityDarkSecondaryContainer,
    onSecondaryContainer = HighDensityDarkOnSecondaryContainer,
    tertiary = HighDensityDarkTertiary,
    tertiaryContainer = HighDensityDarkTertiaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    background = HighDensityBackground,
    onBackground = HighDensityOnBackground,
    surface = HighDensitySurface,
    onSurface = HighDensityOnSurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityOnSurfaceVariant,
    outline = HighDensityOutline,
    outlineVariant = HighDensityOutlineVariant,
    secondary = HighDensitySecondary,
    onSecondary = HighDensityOnSecondary,
    secondaryContainer = HighDensitySecondaryContainer,
    onSecondaryContainer = HighDensityOnSecondaryContainer,
    tertiary = HighDensityTertiary,
    onTertiary = HighDensityOnTertiary,
    tertiaryContainer = HighDensityTertiaryContainer,
    onTertiaryContainer = HighDensityOnTertiaryContainer,
    error = HighDensityError,
    onError = HighDensityOnError,
    errorContainer = HighDensityErrorContainer,
    onErrorContainer = HighDensityOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    primaryColor: androidx.compose.ui.graphics.Color? = null,
    secondaryColor: androidx.compose.ui.graphics.Color? = null,
    content: @Composable () -> Unit,
) {
    val baseScheme = LightColorScheme
    val colorScheme = if (primaryColor != null || secondaryColor != null) {
        baseScheme.copy(
            primary = primaryColor ?: baseScheme.primary,
            onPrimary = if (primaryColor != null) androidx.compose.ui.graphics.Color.White else baseScheme.onPrimary,
            primaryContainer = primaryColor?.copy(alpha = 0.12f) ?: baseScheme.primaryContainer,
            onPrimaryContainer = primaryColor ?: baseScheme.onPrimaryContainer,
            secondary = secondaryColor ?: baseScheme.secondary,
            onSecondary = if (secondaryColor != null) androidx.compose.ui.graphics.Color.White else baseScheme.onSecondary,
            secondaryContainer = secondaryColor?.copy(alpha = 0.12f) ?: baseScheme.secondaryContainer,
            onSecondaryContainer = secondaryColor ?: baseScheme.onSecondaryContainer,
            tertiary = primaryColor ?: baseScheme.tertiary,
            tertiaryContainer = primaryColor?.copy(alpha = 0.08f) ?: baseScheme.tertiaryContainer
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
