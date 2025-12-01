package com.example.rickandmorty.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rickandmorty.data.repository.CharacterRepository
import com.example.rickandmorty.data.settings.SettingsDataStore
import com.example.rickandmorty.data.settings.SettingsPreferences
import com.example.rickandmorty.data.settings.ThemeManager
import com.example.rickandmorty.databinding.FragmentSettingsBinding
import com.example.rickandmorty.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val dataStore by lazy { SettingsDataStore(requireContext()) }
    private val sharedPreferences by lazy { SettingsPreferences(requireContext()) }

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
        setupFields()
        setupListeners()
        observeSettings()
        updateBackupInfo()
    }

    private fun setupFields() {
        binding.emailInput.setText(sharedPreferences.getEmail())
        binding.nicknameInput.setText(sharedPreferences.getNickname())
        binding.backupNameInput.setText(sharedPreferences.getBackupName())
        binding.restoreBackupButton.isEnabled = hasInternalBackup()
    }

    private fun setupListeners() {
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                dataStore.setNotifications(isChecked)
            }
        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                dataStore.setDarkTheme(isChecked)
            }
            ThemeManager.applyTheme(isChecked)
        }

        binding.fontSizeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) updateFontPreview(value)
        }

        binding.saveButton.setOnClickListener {
            saveInputs()
        }

        binding.createBackupButton.setOnClickListener {
            saveInputs()
            createBackupFile()
        }

        binding.deleteBackupButton.setOnClickListener {
            saveInputs()
            deleteBackupFile()
        }

        binding.restoreBackupButton.setOnClickListener {
            saveInputs()
            restoreBackupFile()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataStore.settingsFlow.collect { state ->
                    binding.notificationsSwitch.isChecked = state.notificationsEnabled
                    binding.themeSwitch.isChecked = state.darkThemeEnabled
                    binding.fontSizeSlider.value = state.fontSize
                    updateFontPreview(state.fontSize)
                }
            }
        }
    }

    private fun updateFontPreview(size: Float) {
        binding.fontPreview.textSize = size
        binding.fontPreview.text = "Размер текста: ${size.toInt()}sp"
    }

    private fun saveInputs() {
        val email = binding.emailInput.text?.toString().orEmpty()
        val nickname = binding.nicknameInput.text?.toString().orEmpty()
        val backupName = binding.backupNameInput.text?.toString().orEmpty().ifBlank {
            sharedPreferences.getBackupName()
        }

        sharedPreferences.saveUserData(email, nickname, backupName)

        viewLifecycleOwner.lifecycleScope.launch {
            dataStore.setFontSize(binding.fontSizeSlider.value)
        }

        Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
    }

    private fun createBackupFile() {
        binding.createBackupButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val filename = binding.backupNameInput.text?.toString().orEmpty().ifBlank {
                    sharedPreferences.getBackupName()
                }
                val targetFile = File(getPublicFolder(), filename)
                targetFile.parentFile?.mkdirs()

                val characters = CharacterRepository.getCharacters(1)
                val content = characters?.results?.joinToString(separator = "\n") {
                    "${it.name} — ${it.status} — ${it.species}"
                } ?: "Нет данных для сохранения"

                FileOutputStream(targetFile).use { stream ->
                    stream.write(content.toByteArray())
                    stream.flush()
                }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    binding.createBackupButton.isEnabled = true
                    updateBackupInfo()
                    Toast.makeText(requireContext(), "Файл сохранён в загрузках", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                withContext(Dispatchers.Main) {
                    binding.createBackupButton.isEnabled = true
                    Toast.makeText(requireContext(), "Ошибка сохранения: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteBackupFile() {
        val file = File(getPublicFolder(), sharedPreferences.getBackupName())
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Файл отсутствует", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            cacheInternalCopy(file)
            val deleted = file.delete()
            withContext(Dispatchers.Main) {
                updateBackupInfo()
                binding.restoreBackupButton.isEnabled = hasInternalBackup()
                val message = if (deleted) "Файл удалён" else "Не удалось удалить"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restoreBackupFile() {
        val internalCopy = getInternalCopy()
        if (!internalCopy.exists()) {
            Toast.makeText(requireContext(), "Резервная копия не найдена", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val destination = File(getPublicFolder(), sharedPreferences.getBackupName())
            destination.parentFile?.mkdirs()
            internalCopy.copyTo(destination, overwrite = true)
            withContext(Dispatchers.Main) {
                updateBackupInfo()
                binding.restoreBackupButton.isEnabled = hasInternalBackup()
                Toast.makeText(requireContext(), "Файл восстановлен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cacheInternalCopy(file: File) {
        val backupCopy = getInternalCopy()
        backupCopy.parentFile?.mkdirs()
        file.copyTo(backupCopy, overwrite = true)
    }

    private fun updateBackupInfo() {
        val file = File(getPublicFolder(), sharedPreferences.getBackupName())
        if (file.exists()) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val modified = dateFormat.format(Date(file.lastModified()))
            val sizeKb = file.length() / 1024
            binding.backupInfo.text = "Файл: ${file.name}\nПуть: ${file.parent}\nРазмер: ${sizeKb} КБ\nОбновлён: $modified"
            binding.deleteBackupButton.isEnabled = true
        } else {
            binding.backupInfo.text = "Файл не создан"
            binding.deleteBackupButton.isEnabled = false
        }

        binding.internalBackupInfo.isVisible = hasInternalBackup()
        if (hasInternalBackup()) {
            val cache = getInternalCopy()
            val sizeKb = cache.length() / 1024
            binding.internalBackupInfo.text = "Резервная копия сохранена (${sizeKb} КБ)"
        } else {
            binding.internalBackupInfo.text = "Резервная копия отсутствует"
        }
    }

    private fun hasInternalBackup(): Boolean = getInternalCopy().exists()

    private fun getInternalCopy(): File = File(requireContext().filesDir, "backup_cache/${sharedPreferences.getBackupName()}")

    private fun getPublicFolder(): File = android.os.Environment
        .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
