package com.example.rickandmorty.ui.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.rickandmorty.R
import com.example.rickandmorty.data.prefs.DataStoreManager
import com.example.rickandmorty.ui.base.BaseActivity
import com.example.rickandmorty.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromSettings()

        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d(logTag, "onCreate() завершён")

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container) as? NavHostFragment

            if (navHostFragment != null) {
                navController = navHostFragment.navController
            } else {
                Log.e(logTag, "NavHostFragment не найден")
            }
        } catch (e: Exception) {
            Log.e(logTag, "Ошибка в onCreate: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun applyThemeFromSettings() {
        try {
            val dataStoreManager = DataStoreManager(this)

            runBlocking {
                try {
                    val theme = dataStoreManager.themeFlow.first()
                    when (theme) {
                        "dark" -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            Log.d(logTag, "Применена темная тема")
                        }
                        "light" -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            Log.d(logTag, "Применена светлая тема")
                        }
                        "system" -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            Log.d(logTag, "Применена системная тема")
                        }
                        else -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            Log.d(logTag, "Применена тема по умолчанию (темная)")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(logTag, "Ошибка чтения темы из DataStore: ${e.message}")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Критическая ошибка применения темы: ${e.message}")
            e.printStackTrace()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}