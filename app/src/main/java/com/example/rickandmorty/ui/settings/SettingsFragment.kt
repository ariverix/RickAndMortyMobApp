package com.example.rickandmorty.ui.settings

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.rickandmorty.R
import com.example.rickandmorty.data.prefs.DataStoreManager
import com.example.rickandmorty.data.prefs.SharedPrefsManager
import com.example.rickandmorty.data.repository.CharacterRepository
import com.example.rickandmorty.data.storage.FileManager
import com.example.rickandmorty.databinding.FragmentSettingsBinding
import com.example.rickandmorty.ui.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var fileManager: FileManager
    private lateinit var repository: CharacterRepository

    private val CHANNEL_ID = "rick_morty_channel"
    private val NOTIFICATION_ID = 1001

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(requireContext(), "Разрешения предоставлены", Toast.LENGTH_SHORT).show()
            updateFileInfo()
        } else {
            Toast.makeText(requireContext(), "Разрешения не предоставлены", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendTestNotification()
        } else {
            Toast.makeText(requireContext(), "Разрешение на уведомления не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())
        sharedPrefsManager = SharedPrefsManager(requireContext())
        fileManager = FileManager(requireContext())
        repository = CharacterRepository(requireContext())

        createNotificationChannel()
        checkAndRequestPermissions()
        setupUI()
        loadSettings()
        setupListeners()
        updateFileInfo()
        updateDatabaseInfo()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rick and Morty Updates"
            val descriptionText = "Уведомления о новых персонажах и обновлениях"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun setupUI() {
        val themeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Темная", "Светлая", "Системная")
        )
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTheme.adapter = themeAdapter
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val theme = dataStoreManager.themeFlow.first()

            binding.spinnerTheme.setSelection(
                when (theme) {
                    "dark" -> 0
                    "light" -> 1
                    else -> 2
                }
            )

            binding.etEmail.setText(sharedPrefsManager.userEmail)
            binding.switchNotifications.isChecked = sharedPrefsManager.notificationsEnabled
            binding.etBackupFilename.setText(sharedPrefsManager.backupFilename)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkNotificationPermissionAndSend()
            }
        }

        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        binding.btnCreateFile.setOnClickListener {
            createDataFile()
        }

        binding.btnDeleteFile.setOnClickListener {
            deleteFileWithBackup()
        }

        binding.btnRestoreBackup.setOnClickListener {
            restoreFromBackup()
        }

        binding.btnDeleteBackup.setOnClickListener {
            deleteBackup()
        }

        // Новые кнопки для управления БД
        binding.btnClearDatabase.setOnClickListener {
            showClearDatabaseDialog()
        }

        binding.btnShowDatabaseInfo.setOnClickListener {
            updateDatabaseInfo()
        }
    }

    private fun saveSettings() {
        lifecycleScope.launch {
            try {
                val selectedTheme = when (binding.spinnerTheme.selectedItemPosition) {
                    0 -> "dark"
                    1 -> "light"
                    else -> "system"
                }
                dataStoreManager.saveTheme(selectedTheme)
                logEvent("Тема сохранена: $selectedTheme")

                sharedPrefsManager.userEmail = binding.etEmail.text.toString()
                sharedPrefsManager.notificationsEnabled = binding.switchNotifications.isChecked

                val newFilename = binding.etBackupFilename.text.toString().trim()
                if (newFilename.isNotEmpty() && newFilename.endsWith(".txt")) {
                    sharedPrefsManager.backupFilename = newFilename
                } else if (newFilename.isNotEmpty()) {
                    sharedPrefsManager.backupFilename = "$newFilename.txt"
                }

                Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()

                delay(300)
                applyTheme(selectedTheme)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                logEvent("Ошибка сохранения настроек: ${e.message}")
            }
        }
    }

    private fun applyTheme(theme: String) {
        try {
            when (theme) {
                "dark" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    logEvent("Применена темная тема")
                }
                "light" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    logEvent("Применена светлая тема")
                }
                "system" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    logEvent("Применена системная тема")
                }
            }
        } catch (e: Exception) {
            logEvent("Ошибка применения темы: ${e.message}")
        }
    }

    private fun checkNotificationPermissionAndSend() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                sendTestNotification()
            }
        } else {
            sendTestNotification()
        }
    }

    private fun sendTestNotification() {
        try {
            val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Wubba Lubba Dub-Dub!")
                .setContentText("Уведомления включены! Теперь вы не пропустите новых персонажей")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(requireContext())) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                ) {
                    notify(NOTIFICATION_ID, builder.build())
                    Toast.makeText(requireContext(), "Тестовое уведомление отправлено!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка отправки уведомления: ${e.message}", Toast.LENGTH_SHORT).show()
            logEvent("Ошибка уведомления: ${e.message}")
        }
    }

    private fun createDataFile() {
        lifecycleScope.launch {
            try {
                val testCharacters = listOf(
                    com.example.rickandmorty.ui.home.CharacterUi(
                        "Rick Sanchez",
                        "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
                        "Alive",
                        "Human"
                    ),
                    com.example.rickandmorty.ui.home.CharacterUi(
                        "Morty Smith",
                        "https://rickandmortyapi.com/api/character/avatar/2.jpeg",
                        "Alive",
                        "Human"
                    )
                )

                val filename = sharedPrefsManager.backupFilename
                logEvent("Попытка создания файла: $filename")

                val success = fileManager.saveCharactersToExternal(testCharacters, filename)

                if (success) {
                    Toast.makeText(requireContext(), "Файл создан успешно", Toast.LENGTH_SHORT).show()
                    logEvent("Файл $filename создан во внешнем хранилище")
                    updateFileInfo()
                } else {
                    Toast.makeText(requireContext(), "Ошибка создания файла", Toast.LENGTH_LONG).show()
                    logEvent("Ошибка создания файла")
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                logEvent("Ошибка создания файла: ${e.message}")
            }
        }
    }

    private fun deleteFileWithBackup() {
        lifecycleScope.launch {
            try {
                val filename = sharedPrefsManager.backupFilename
                val fileExists = fileManager.getExternalFileInfo(filename).exists

                if (!fileExists) {
                    Toast.makeText(requireContext(), "Файл не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val backupSuccess = fileManager.createBackup(filename)
                if (!backupSuccess) {
                    Toast.makeText(requireContext(), "Ошибка создания резервной копии", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val deleteSuccess = fileManager.deleteExternalFile(filename)
                if (deleteSuccess) {
                    Toast.makeText(requireContext(), "Файл удален, резервная копия сохранена", Toast.LENGTH_SHORT).show()
                    updateFileInfo()
                } else {
                    fileManager.deleteBackup(filename)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun restoreFromBackup() {
        lifecycleScope.launch {
            try {
                val filename = sharedPrefsManager.backupFilename
                val success = fileManager.restoreFromBackup(filename)

                if (success) {
                    Toast.makeText(requireContext(), "Файл восстановлен", Toast.LENGTH_SHORT).show()
                    updateFileInfo()
                } else {
                    Toast.makeText(requireContext(), "Резервная копия не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteBackup() {
        lifecycleScope.launch {
            try {
                val filename = sharedPrefsManager.backupFilename
                val success = fileManager.deleteBackup(filename)

                if (success) {
                    Toast.makeText(requireContext(), "Резервная копия удалена", Toast.LENGTH_SHORT).show()
                    updateFileInfo()
                } else {
                    Toast.makeText(requireContext(), "Резервная копия не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFileInfo() {
        lifecycleScope.launch {
            try {
                val filename = sharedPrefsManager.backupFilename

                val externalInfo = fileManager.getExternalFileInfo(filename)
                if (externalInfo.exists) {
                    binding.tvFileInfo.text = buildString {
                        appendLine("Файл данных:")
                        appendLine("Название: ${externalInfo.name}")
                        appendLine("Размер: ${fileManager.formatFileSize(externalInfo.size)}")
                    }
                    binding.tvFileInfo.visibility = View.VISIBLE
                    binding.btnCreateFile.isEnabled = false
                    binding.btnDeleteFile.isEnabled = true
                } else {
                    binding.tvFileInfo.text = "Файл данных отсутствует"
                    binding.tvFileInfo.visibility = View.VISIBLE
                    binding.btnCreateFile.isEnabled = true
                    binding.btnDeleteFile.isEnabled = false
                }

                val backupInfo = fileManager.getBackupFileInfo(filename)
                if (backupInfo.exists) {
                    binding.tvBackupInfo.text = buildString {
                        appendLine("Резервная копия:")
                        appendLine("Размер: ${fileManager.formatFileSize(backupInfo.size)}")
                    }
                    binding.tvBackupInfo.visibility = View.VISIBLE
                    binding.btnRestoreBackup.isEnabled = true
                    binding.btnDeleteBackup.isEnabled = true
                } else {
                    binding.tvBackupInfo.text = "Резервная копия отсутствует"
                    binding.tvBackupInfo.visibility = View.VISIBLE
                    binding.btnRestoreBackup.isEnabled = false
                    binding.btnDeleteBackup.isEnabled = false
                }
            } catch (e: Exception) {
                logEvent("Ошибка обновления информации о файлах: ${e.message}")
            }
        }
    }

    // Новые методы для управления БД
    private fun updateDatabaseInfo() {
        lifecycleScope.launch {
            try {
                val count = repository.isDatabaseEmpty()
                val characters = repository.getCharactersFromDb()

                binding.tvDatabaseInfo.text = buildString {
                    appendLine("База данных Room:")
                    appendLine("Персонажей в БД: ${characters.size}")
                    appendLine("Статус: ${if (count) "Пустая" else "Содержит данные"}")
                }
                binding.tvDatabaseInfo.visibility = View.VISIBLE

                binding.btnClearDatabase.isEnabled = !count
            } catch (e: Exception) {
                binding.tvDatabaseInfo.text = "Ошибка чтения БД: ${e.message}"
                binding.tvDatabaseInfo.visibility = View.VISIBLE
            }
        }
    }

    private fun showClearDatabaseDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистка базы данных")
            .setMessage("Вы уверены, что хотите удалить все данные из базы данных?")
            .setPositiveButton("Да") { _, _ ->
                clearDatabase()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearDatabase() {
        lifecycleScope.launch {
            try {
                repository.clearDatabase()
                Toast.makeText(requireContext(), "База данных очищена", Toast.LENGTH_SHORT).show()
                updateDatabaseInfo()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}