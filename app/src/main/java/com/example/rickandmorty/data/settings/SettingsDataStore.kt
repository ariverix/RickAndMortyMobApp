package com.example.rickandmorty.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "user_settings")

data class SettingsState(
    val notificationsEnabled: Boolean,
    val darkThemeEnabled: Boolean,
    val fontSize: Float
)

class SettingsDataStore(private val context: Context) {

    val settingsFlow: Flow<SettingsState> = context.settingsDataStore.data.map { prefs ->
        SettingsState(
            notificationsEnabled = prefs[NOTIFICATIONS_KEY] ?: true,
            darkThemeEnabled = prefs[DARK_THEME_KEY] ?: false,
            fontSize = prefs[FONT_SIZE_KEY] ?: 16f
        )
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun setFontSize(size: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[FONT_SIZE_KEY] = size
        }
    }

    companion object {
        val NOTIFICATIONS_KEY: Preferences.Key<Boolean> = booleanPreferencesKey("notifications_enabled")
        val DARK_THEME_KEY: Preferences.Key<Boolean> = booleanPreferencesKey("dark_theme_enabled")
        val FONT_SIZE_KEY: Preferences.Key<Float> = floatPreferencesKey("font_size")
    }
}
