package com.example.rickandmorty.data.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object ThemeManager {
    fun applySavedTheme(context: Context) {
        runBlocking {
            val prefs = context.settingsDataStore.data.first()
            val isDark = prefs[SettingsDataStore.DARK_THEME_KEY] ?: false
            applyTheme(isDark)
        }
    }

    fun applyTheme(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
