package com.example.rickandmorty.data.settings

import android.content.Context

class SettingsPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("user_shared_settings", Context.MODE_PRIVATE)

    fun saveUserData(email: String, nickname: String, backupName: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_BACKUP_NAME, backupName)
            .apply()
    }

    fun getEmail(): String = prefs.getString(KEY_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL

    fun getNickname(): String = prefs.getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME

    fun getBackupName(): String = prefs.getString(KEY_BACKUP_NAME, DEFAULT_BACKUP_NAME) ?: DEFAULT_BACKUP_NAME

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_BACKUP_NAME = "backup_file_name"

        private const val DEFAULT_EMAIL = ""
        private const val DEFAULT_NICKNAME = ""
        private const val DEFAULT_BACKUP_NAME = "backup_01.txt"
    }
}
