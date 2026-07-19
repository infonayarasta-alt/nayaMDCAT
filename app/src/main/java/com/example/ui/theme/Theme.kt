package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityDarkPrimary,
    onPrimary = HighDensityDarkOnPrimary,
    primaryContainer = HighDensityDarkPrimaryContainer,
    onPrimaryContainer = HighDensityDarkOnPrimaryContainer,
    inversePrimary = HighDensityDarkInversePrimary,
    secondary = HighDensityDarkSecondary,
    onSecondary = HighDensityDarkOnSecondary,
    secondaryContainer = HighDensityDarkSecondaryContainer,
    onSecondaryContainer = HighDensityDarkOnSecondaryContainer,
    tertiary = HighDensityDarkTertiary,
    onTertiary = HighDensityDarkOnTertiary,
    tertiaryContainer = HighDensityDarkTertiaryContainer,
    onTertiaryContainer = HighDensityDarkOnTertiaryContainer,
    error = HighDensityDarkError,
    onError = HighDensityDarkOnError,
    errorContainer = HighDensityDarkErrorContainer,
    onErrorContainer = HighDensityDarkOnErrorContainer,
    background = HighDensityDarkBackground,
    onBackground = HighDensityDarkOnBackground,
    surface = HighDensityDarkSurface,
    onSurface = HighDensityDarkOnSurface,
    surfaceVariant = HighDensityDarkSurfaceVariant,
    onSurfaceVariant = HighDensityDarkOnSurfaceVariant,
    outline = HighDensityDarkOutline,
    outlineVariant = HighDensityDarkOutlineVariant,
    scrim = HighDensityDarkScrim,
    inverseSurface = HighDensityDarkInverseSurface,
    inverseOnSurface = HighDensityDarkInverseOnSurface,
    surfaceDim = HighDensityDarkSurfaceDim,
    surfaceBright = HighDensityDarkSurfaceBright,
    surfaceContainerLowest = HighDensityDarkSurfaceContainerLowest,
    surfaceContainerLow = HighDensityDarkSurfaceContainerLow,
    surfaceContainer = HighDensityDarkSurfaceContainer,
    surfaceContainerHigh = HighDensityDarkSurfaceContainerHigh,
    surfaceContainerHighest = HighDensityDarkSurfaceContainerHighest,
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    inversePrimary = HighDensityInversePrimary,
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
    onErrorContainer = HighDensityOnErrorContainer,
    background = HighDensityBackground,
    onBackground = HighDensityOnBackground,
    surface = HighDensitySurface,
    onSurface = HighDensityOnSurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityOnSurfaceVariant,
    outline = HighDensityOutline,
    outlineVariant = HighDensityOutlineVariant,
    scrim = HighDensityScrim,
    inverseSurface = HighDensityInverseSurface,
    inverseOnSurface = HighDensityInverseOnSurface,
    surfaceDim = HighDensitySurfaceDim,
    surfaceBright = HighDensitySurfaceBright,
    surfaceContainerLowest = HighDensitySurfaceContainerLowest,
    surfaceContainerLow = HighDensitySurfaceContainerLow,
    surfaceContainer = HighDensitySurfaceContainer,
    surfaceContainerHigh = HighDensitySurfaceContainerHigh,
    surfaceContainerHighest = HighDensitySurfaceContainerHighest,
)

/**
 * App theme. `darkTheme` now actually drives the resolved scheme (previously
 * always light regardless of caller). An optional admin-configured brand
 * override (primary/secondary) is layered on top, preserving the existing
 * "custom theme color" feature end to end.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    primaryColor: androidx.compose.ui.graphics.Color? = null,
    secondaryColor: androidx.compose.ui.graphics.Color? = null,
    content: @Composable () -> Unit,
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val colorScheme = if (primaryColor != null || secondaryColor != null) {
        val onOverride = androidx.compose.ui.graphics.Color.White
        val containerAlpha = if (darkTheme) 0.24f else 0.12f
        baseScheme.copy(
            primary = primaryColor ?: baseScheme.primary,
            onPrimary = if (primaryColor != null) onOverride else baseScheme.onPrimary,
            primaryContainer = primaryColor?.copy(alpha = containerAlpha) ?: baseScheme.primaryContainer,
            onPrimaryContainer = primaryColor ?: baseScheme.onPrimaryContainer,
            secondary = secondaryColor ?: baseScheme.secondary,
            onSecondary = if (secondaryColor != null) onOverride else baseScheme.onSecondary,
            secondaryContainer = secondaryColor?.copy(alpha = containerAlpha) ?: baseScheme.secondaryContainer,
            onSecondaryContainer = secondaryColor ?: baseScheme.onSecondaryContainer,
            tertiary = primaryColor ?: baseScheme.tertiary,
            tertiaryContainer = primaryColor?.copy(alpha = if (darkTheme) 0.20f else 0.08f) ?: baseScheme.tertiaryContainer,
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
