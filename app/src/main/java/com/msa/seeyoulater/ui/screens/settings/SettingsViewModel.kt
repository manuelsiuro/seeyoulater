package com.msa.seeyoulater.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.preferences.ColorScheme
import com.msa.seeyoulater.data.preferences.ThemeMode
import com.msa.seeyoulater.data.preferences.ThemePreferencesRepository
import com.msa.seeyoulater.data.preferences.ThemeSettings
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen managing app preferences
 */
class SettingsViewModel(
    private val repository: LinkRepository,
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    /**
     * Current theme settings as StateFlow
     */
    val themeSettings: StateFlow<ThemeSettings> = themePreferencesRepository.themeSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeSettings() // Default settings
    )

    /**
     * Update theme mode (Light/Dark/System)
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                themePreferencesRepository.updateThemeMode(themeMode)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    /**
     * Update color scheme
     */
    fun updateColorScheme(colorScheme: ColorScheme) {
        viewModelScope.launch {
            try {
                themePreferencesRepository.updateColorScheme(colorScheme)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    /**
     * Update both theme mode and color scheme
     */
    fun updateThemeSettings(themeSettings: ThemeSettings) {
        viewModelScope.launch {
            try {
                themePreferencesRepository.updateThemeSettings(themeSettings)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    /**
     * Reset theme to defaults
     */
    fun resetThemeToDefaults() {
        viewModelScope.launch {
            try {
                themePreferencesRepository.resetToDefaults()
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    /**
     * Clear all saved links from the database
     */
    fun clearAllLinks() {
        viewModelScope.launch {
            try {
                repository.deleteAllLinks()
                // Optionally emit a state update or event
            } catch (e: Exception) {
                // Handle error (e.g., emit error state)
                e.printStackTrace()
            }
        }
    }
}
