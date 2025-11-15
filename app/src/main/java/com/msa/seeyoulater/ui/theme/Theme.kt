package com.msa.seeyoulater.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.msa.seeyoulater.data.preferences.ColorScheme as ThemeColorScheme

// Classic Theme (Purple) - Light Mode
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

// Classic Theme (Purple) - Dark Mode
private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

// Ocean Theme (Blue) - Light Mode
private val OceanLightColorScheme = lightColorScheme(
    primary = md_theme_ocean_light_primary,
    onPrimary = md_theme_ocean_light_onPrimary,
    primaryContainer = md_theme_ocean_light_primaryContainer,
    onPrimaryContainer = md_theme_ocean_light_onPrimaryContainer,
    secondary = md_theme_ocean_light_secondary,
    onSecondary = md_theme_ocean_light_onSecondary,
    secondaryContainer = md_theme_ocean_light_secondaryContainer,
    onSecondaryContainer = md_theme_ocean_light_onSecondaryContainer,
    tertiary = md_theme_ocean_light_tertiary,
    onTertiary = md_theme_ocean_light_onTertiary,
    tertiaryContainer = md_theme_ocean_light_tertiaryContainer,
    onTertiaryContainer = md_theme_ocean_light_onTertiaryContainer,
    error = md_theme_ocean_light_error,
    errorContainer = md_theme_ocean_light_errorContainer,
    onError = md_theme_ocean_light_onError,
    onErrorContainer = md_theme_ocean_light_onErrorContainer,
    background = md_theme_ocean_light_background,
    onBackground = md_theme_ocean_light_onBackground,
    surface = md_theme_ocean_light_surface,
    onSurface = md_theme_ocean_light_onSurface,
    surfaceVariant = md_theme_ocean_light_surfaceVariant,
    onSurfaceVariant = md_theme_ocean_light_onSurfaceVariant,
    outline = md_theme_ocean_light_outline,
    inverseOnSurface = md_theme_ocean_light_inverseOnSurface,
    inverseSurface = md_theme_ocean_light_inverseSurface,
    inversePrimary = md_theme_ocean_light_inversePrimary,
    surfaceTint = md_theme_ocean_light_surfaceTint,
    outlineVariant = md_theme_ocean_light_outlineVariant,
    scrim = md_theme_ocean_light_scrim,
)

// Ocean Theme (Blue) - Dark Mode
private val OceanDarkColorScheme = darkColorScheme(
    primary = md_theme_ocean_dark_primary,
    onPrimary = md_theme_ocean_dark_onPrimary,
    primaryContainer = md_theme_ocean_dark_primaryContainer,
    onPrimaryContainer = md_theme_ocean_dark_onPrimaryContainer,
    secondary = md_theme_ocean_dark_secondary,
    onSecondary = md_theme_ocean_dark_onSecondary,
    secondaryContainer = md_theme_ocean_dark_secondaryContainer,
    onSecondaryContainer = md_theme_ocean_dark_onSecondaryContainer,
    tertiary = md_theme_ocean_dark_tertiary,
    onTertiary = md_theme_ocean_dark_onTertiary,
    tertiaryContainer = md_theme_ocean_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_ocean_dark_onTertiaryContainer,
    error = md_theme_ocean_dark_error,
    errorContainer = md_theme_ocean_dark_errorContainer,
    onError = md_theme_ocean_dark_onError,
    onErrorContainer = md_theme_ocean_dark_onErrorContainer,
    background = md_theme_ocean_dark_background,
    onBackground = md_theme_ocean_dark_onBackground,
    surface = md_theme_ocean_dark_surface,
    onSurface = md_theme_ocean_dark_onSurface,
    surfaceVariant = md_theme_ocean_dark_surfaceVariant,
    onSurfaceVariant = md_theme_ocean_dark_onSurfaceVariant,
    outline = md_theme_ocean_dark_outline,
    inverseOnSurface = md_theme_ocean_dark_inverseOnSurface,
    inverseSurface = md_theme_ocean_dark_inverseSurface,
    inversePrimary = md_theme_ocean_dark_inversePrimary,
    surfaceTint = md_theme_ocean_dark_surfaceTint,
    outlineVariant = md_theme_ocean_dark_outlineVariant,
    scrim = md_theme_ocean_dark_scrim,
)

// Forest Theme (Green) - Light Mode
private val ForestLightColorScheme = lightColorScheme(
    primary = md_theme_forest_light_primary,
    onPrimary = md_theme_forest_light_onPrimary,
    primaryContainer = md_theme_forest_light_primaryContainer,
    onPrimaryContainer = md_theme_forest_light_onPrimaryContainer,
    secondary = md_theme_forest_light_secondary,
    onSecondary = md_theme_forest_light_onSecondary,
    secondaryContainer = md_theme_forest_light_secondaryContainer,
    onSecondaryContainer = md_theme_forest_light_onSecondaryContainer,
    tertiary = md_theme_forest_light_tertiary,
    onTertiary = md_theme_forest_light_onTertiary,
    tertiaryContainer = md_theme_forest_light_tertiaryContainer,
    onTertiaryContainer = md_theme_forest_light_onTertiaryContainer,
    error = md_theme_forest_light_error,
    errorContainer = md_theme_forest_light_errorContainer,
    onError = md_theme_forest_light_onError,
    onErrorContainer = md_theme_forest_light_onErrorContainer,
    background = md_theme_forest_light_background,
    onBackground = md_theme_forest_light_onBackground,
    surface = md_theme_forest_light_surface,
    onSurface = md_theme_forest_light_onSurface,
    surfaceVariant = md_theme_forest_light_surfaceVariant,
    onSurfaceVariant = md_theme_forest_light_onSurfaceVariant,
    outline = md_theme_forest_light_outline,
    inverseOnSurface = md_theme_forest_light_inverseOnSurface,
    inverseSurface = md_theme_forest_light_inverseSurface,
    inversePrimary = md_theme_forest_light_inversePrimary,
    surfaceTint = md_theme_forest_light_surfaceTint,
    outlineVariant = md_theme_forest_light_outlineVariant,
    scrim = md_theme_forest_light_scrim,
)

// Forest Theme (Green) - Dark Mode
private val ForestDarkColorScheme = darkColorScheme(
    primary = md_theme_forest_dark_primary,
    onPrimary = md_theme_forest_dark_onPrimary,
    primaryContainer = md_theme_forest_dark_primaryContainer,
    onPrimaryContainer = md_theme_forest_dark_onPrimaryContainer,
    secondary = md_theme_forest_dark_secondary,
    onSecondary = md_theme_forest_dark_onSecondary,
    secondaryContainer = md_theme_forest_dark_secondaryContainer,
    onSecondaryContainer = md_theme_forest_dark_onSecondaryContainer,
    tertiary = md_theme_forest_dark_tertiary,
    onTertiary = md_theme_forest_dark_onTertiary,
    tertiaryContainer = md_theme_forest_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_forest_dark_onTertiaryContainer,
    error = md_theme_forest_dark_error,
    errorContainer = md_theme_forest_dark_errorContainer,
    onError = md_theme_forest_dark_onError,
    onErrorContainer = md_theme_forest_dark_onErrorContainer,
    background = md_theme_forest_dark_background,
    onBackground = md_theme_forest_dark_onBackground,
    surface = md_theme_forest_dark_surface,
    onSurface = md_theme_forest_dark_onSurface,
    surfaceVariant = md_theme_forest_dark_surfaceVariant,
    onSurfaceVariant = md_theme_forest_dark_onSurfaceVariant,
    outline = md_theme_forest_dark_outline,
    inverseOnSurface = md_theme_forest_dark_inverseOnSurface,
    inverseSurface = md_theme_forest_dark_inverseSurface,
    inversePrimary = md_theme_forest_dark_inversePrimary,
    surfaceTint = md_theme_forest_dark_surfaceTint,
    outlineVariant = md_theme_forest_dark_outlineVariant,
    scrim = md_theme_forest_dark_scrim,
)

// Sunset Theme (Orange) - Light Mode
private val SunsetLightColorScheme = lightColorScheme(
    primary = md_theme_sunset_light_primary,
    onPrimary = md_theme_sunset_light_onPrimary,
    primaryContainer = md_theme_sunset_light_primaryContainer,
    onPrimaryContainer = md_theme_sunset_light_onPrimaryContainer,
    secondary = md_theme_sunset_light_secondary,
    onSecondary = md_theme_sunset_light_onSecondary,
    secondaryContainer = md_theme_sunset_light_secondaryContainer,
    onSecondaryContainer = md_theme_sunset_light_onSecondaryContainer,
    tertiary = md_theme_sunset_light_tertiary,
    onTertiary = md_theme_sunset_light_onTertiary,
    tertiaryContainer = md_theme_sunset_light_tertiaryContainer,
    onTertiaryContainer = md_theme_sunset_light_onTertiaryContainer,
    error = md_theme_sunset_light_error,
    errorContainer = md_theme_sunset_light_errorContainer,
    onError = md_theme_sunset_light_onError,
    onErrorContainer = md_theme_sunset_light_onErrorContainer,
    background = md_theme_sunset_light_background,
    onBackground = md_theme_sunset_light_onBackground,
    surface = md_theme_sunset_light_surface,
    onSurface = md_theme_sunset_light_onSurface,
    surfaceVariant = md_theme_sunset_light_surfaceVariant,
    onSurfaceVariant = md_theme_sunset_light_onSurfaceVariant,
    outline = md_theme_sunset_light_outline,
    inverseOnSurface = md_theme_sunset_light_inverseOnSurface,
    inverseSurface = md_theme_sunset_light_inverseSurface,
    inversePrimary = md_theme_sunset_light_inversePrimary,
    surfaceTint = md_theme_sunset_light_surfaceTint,
    outlineVariant = md_theme_sunset_light_outlineVariant,
    scrim = md_theme_sunset_light_scrim,
)

// Sunset Theme (Orange) - Dark Mode
private val SunsetDarkColorScheme = darkColorScheme(
    primary = md_theme_sunset_dark_primary,
    onPrimary = md_theme_sunset_dark_onPrimary,
    primaryContainer = md_theme_sunset_dark_primaryContainer,
    onPrimaryContainer = md_theme_sunset_dark_onPrimaryContainer,
    secondary = md_theme_sunset_dark_secondary,
    onSecondary = md_theme_sunset_dark_onSecondary,
    secondaryContainer = md_theme_sunset_dark_secondaryContainer,
    onSecondaryContainer = md_theme_sunset_dark_onSecondaryContainer,
    tertiary = md_theme_sunset_dark_tertiary,
    onTertiary = md_theme_sunset_dark_onTertiary,
    tertiaryContainer = md_theme_sunset_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_sunset_dark_onTertiaryContainer,
    error = md_theme_sunset_dark_error,
    errorContainer = md_theme_sunset_dark_errorContainer,
    onError = md_theme_sunset_dark_onError,
    onErrorContainer = md_theme_sunset_dark_onErrorContainer,
    background = md_theme_sunset_dark_background,
    onBackground = md_theme_sunset_dark_onBackground,
    surface = md_theme_sunset_dark_surface,
    onSurface = md_theme_sunset_dark_onSurface,
    surfaceVariant = md_theme_sunset_dark_surfaceVariant,
    onSurfaceVariant = md_theme_sunset_dark_onSurfaceVariant,
    outline = md_theme_sunset_dark_outline,
    inverseOnSurface = md_theme_sunset_dark_inverseOnSurface,
    inverseSurface = md_theme_sunset_dark_inverseSurface,
    inversePrimary = md_theme_sunset_dark_inversePrimary,
    surfaceTint = md_theme_sunset_dark_surfaceTint,
    outlineVariant = md_theme_sunset_dark_outlineVariant,
    scrim = md_theme_sunset_dark_scrim,
)

@Composable
fun LinkManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ThemeColorScheme = ThemeColorScheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val materialColorScheme = when {
        // Dynamic color (Material You) for Android 12+
        colorScheme == ThemeColorScheme.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Other color schemes
        else -> {
            when (colorScheme) {
                ThemeColorScheme.CLASSIC -> if (darkTheme) DarkColorScheme else LightColorScheme
                ThemeColorScheme.OCEAN -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
                ThemeColorScheme.FOREST -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
                ThemeColorScheme.SUNSET -> if (darkTheme) SunsetDarkColorScheme else SunsetLightColorScheme
                ThemeColorScheme.DYNAMIC -> if (darkTheme) DarkColorScheme else LightColorScheme // Fallback for < Android 12
            }
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Enable edge-to-edge display (mandatory for Android 16+)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Configure system bar appearance for light/dark themes
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = materialColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
