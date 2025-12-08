package com.example.rickandmorty.data.prefs

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_BACKUP_FILENAME = "backup_filename"
    }

    var userEmail: String?
        get() = prefs.getString(KEY_EMAIL, "")
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var backupFilename: String
        get() = prefs.getString(KEY_BACKUP_FILENAME, "backup_data.txt") ?: "backup_data.txt"
        set(value) = prefs.edit().putString(KEY_BACKUP_FILENAME, value).apply()
}