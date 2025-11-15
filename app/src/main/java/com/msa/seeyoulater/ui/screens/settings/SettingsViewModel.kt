package com.msa.seeyoulater.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.launch

// Basic ViewModel for settings, can be expanded
class SettingsViewModel(private val repository: LinkRepository) : ViewModel() {

    // Add StateFlow for settings preferences if they were persisted (e.g., using DataStore)
    // val previewEnabled: StateFlow<Boolean> = ...
    // val currentTheme: StateFlow<ThemePreference> = ...

    fun clearAllLinks() {
        viewModelScope.launch {
            try {
                repository.deleteAllLinks()
                // Optionally emit a state update or event
            } catch (e: Exception) {
                // Handle error (e.g., emit error state)
            }
        }
    }

    // Functions to update settings preferences would go here
    // fun setUrlPreviewEnabled(enabled: Boolean) { ... }
    // fun setTheme(theme: ThemePreference) { ... }
}
