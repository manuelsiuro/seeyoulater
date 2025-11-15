package com.msa.seeyoulater.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing general app preferences using DataStore
 */
class AppPreferencesRepository(private val context: Context) {

    companion object {
        private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "app_preferences"
        )

        private val URL_PREVIEW_ENABLED_KEY = booleanPreferencesKey("url_preview_enabled")
    }

    /**
     * Flow of URL preview enabled state
     */
    val urlPreviewEnabled: Flow<Boolean> = context.appDataStore.data.map { preferences ->
        preferences[URL_PREVIEW_ENABLED_KEY] ?: true // Default: enabled
    }

    /**
     * Update URL preview enabled state
     */
    suspend fun setUrlPreviewEnabled(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[URL_PREVIEW_ENABLED_KEY] = enabled
        }
    }
}
