package com.example.rickandmorty.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "dark"
    }

    val fontSizeFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[FONT_SIZE_KEY] ?: 14
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "ru"
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    suspend fun saveFontSize(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[FONT_SIZE_KEY] = size
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language
        }
    }
}