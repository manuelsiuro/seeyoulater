package com.msa.seeyoulater.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing theme preferences using DataStore
 */
class ThemePreferencesRepository(private val context: Context) {

    companion object {
        private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "theme_preferences"
        )

        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val COLOR_SCHEME_KEY = stringPreferencesKey("color_scheme")
    }

    /**
     * Flow of current theme settings
     */
    val themeSettings: Flow<ThemeSettings> = context.themeDataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        val colorSchemeString = preferences[COLOR_SCHEME_KEY] ?: ColorScheme.CLASSIC.name

        ThemeSettings(
            themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            },
            colorScheme = try {
                ColorScheme.valueOf(colorSchemeString)
            } catch (e: IllegalArgumentException) {
                ColorScheme.CLASSIC
            }
        )
    }

    /**
     * Update the theme mode (Light/Dark/System)
     */
    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    /**
     * Update the color scheme
     */
    suspend fun updateColorScheme(colorScheme: ColorScheme) {
        context.themeDataStore.edit { preferences ->
            preferences[COLOR_SCHEME_KEY] = colorScheme.name
        }
    }

    /**
     * Update both theme mode and color scheme
     */
    suspend fun updateThemeSettings(themeSettings: ThemeSettings) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeSettings.themeMode.name
            preferences[COLOR_SCHEME_KEY] = themeSettings.colorScheme.name
        }
    }

    /**
     * Reset to default theme settings
     */
    suspend fun resetToDefaults() {
        context.themeDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
