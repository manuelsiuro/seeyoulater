package com.msa.seeyoulater.data.preferences

/**
 * Represents the theme mode (light/dark/system)
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Represents the available color schemes
 */
enum class ColorScheme {
    CLASSIC,  // Purple theme (default)
    OCEAN,    // Blue theme
    FOREST,   // Green theme
    SUNSET,   // Orange/Coral theme
    DYNAMIC;  // Material You (Android 12+)

    /**
     * Human-readable display name for the color scheme
     */
    fun displayName(): String = when (this) {
        CLASSIC -> "Classic Purple"
        OCEAN -> "Ocean Blue"
        FOREST -> "Forest Green"
        SUNSET -> "Sunset Orange"
        DYNAMIC -> "Dynamic (Material You)"
    }

    /**
     * Description of the color scheme
     */
    fun description(): String = when (this) {
        CLASSIC -> "The classic purple theme"
        OCEAN -> "Cool and calming blue tones"
        FOREST -> "Natural green palette"
        SUNSET -> "Warm orange and coral colors"
        DYNAMIC -> "Adapts to your wallpaper (Android 12+)"
    }
}

/**
 * Data class representing the user's theme preferences
 */
data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorScheme = ColorScheme.CLASSIC
)
