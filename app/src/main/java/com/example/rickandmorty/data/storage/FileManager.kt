package com.example.rickandmorty.data.storage

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.rickandmorty.ui.home.CharacterUi
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileManager(private val context: Context) {

    private val TAG = "FileManager"

    data class FileInfo(
        val exists: Boolean,
        val name: String = "",
        val path: String = "",
        val size: Long = 0,
        val createdDate: String = ""
    )

    // Внешнее хранилище
    fun getExternalFile(filename: String): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!documentsDir.exists()) {
            val created = documentsDir.mkdirs()
            Log.d(TAG, "Documents directory created: $created")
        }
        return File(documentsDir, filename)
    }

    // Внутреннее хранилище приложения
    fun getInternalBackupFile(filename: String): File {
        return File(context.filesDir, "backup_$filename")
    }

    // Сохранение данных во внешнее хранилище
    fun saveCharactersToExternal(characters: List<CharacterUi>, filename: String): Boolean {
        return try {
            Log.d(TAG, "Attempting to save ${characters.size} characters to $filename")

            val file = getExternalFile(filename)
            Log.d(TAG, "File path: ${file.absolutePath}")

            // Проверка возможность записи
            val canWrite = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            Log.d(TAG, "External storage writable: $canWrite")

            if (!canWrite) {
                Log.e(TAG, "External storage not writable!")
                return false
            }

            val content = buildString {
                appendLine("=== Rick and Morty Characters Data ===")
                appendLine("Total characters: ${characters.size}")
                appendLine("Created: ${getCurrentDateTime()}")
                appendLine("=" .repeat(40))
                appendLine()

                characters.forEachIndexed { index, character ->
                    appendLine("Character ${index + 1}:")
                    appendLine("  Name: ${character.name}")
                    appendLine("  Status: ${character.status}")
                    appendLine("  Species: ${character.species}")
                    appendLine("  Image: ${character.image}")
                    appendLine()
                }
            }

            file.writeText(content)
            Log.d(TAG, "File saved successfully: ${file.exists()}, size: ${file.length()}")

            file.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving file: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    // Получение информации о файле во внешнем хранилище
    fun getExternalFileInfo(filename: String): FileInfo {
        return try {
            val file = getExternalFile(filename)
            Log.d(TAG, "Checking external file: ${file.absolutePath}, exists: ${file.exists()}")

            if (file.exists()) {
                FileInfo(
                    exists = true,
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    createdDate = getFileDate(file.lastModified())
                )
            } else {
                FileInfo(exists = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file info: ${e.message}", e)
            FileInfo(exists = false)
        }
    }

    // Получение информации о резервной копии
    fun getBackupFileInfo(filename: String): FileInfo {
        return try {
            val file = getInternalBackupFile(filename)
            Log.d(TAG, "Checking backup file: ${file.absolutePath}, exists: ${file.exists()}")

            if (file.exists()) {
                FileInfo(
                    exists = true,
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    createdDate = getFileDate(file.lastModified())
                )
            } else {
                FileInfo(exists = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting backup info: ${e.message}", e)
            FileInfo(exists = false)
        }
    }

    // Создание резервной копии (копирование из внешнего во внутреннее)
    fun createBackup(filename: String): Boolean {
        return try {
            val externalFile = getExternalFile(filename)
            val backupFile = getInternalBackupFile(filename)

            Log.d(TAG, "Creating backup from ${externalFile.absolutePath} to ${backupFile.absolutePath}")

            if (externalFile.exists()) {
                externalFile.copyTo(backupFile, overwrite = true)
                val success = backupFile.exists()
                Log.d(TAG, "Backup created: $success")
                success
            } else {
                Log.w(TAG, "External file doesn't exist, cannot create backup")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    // Удаление файла из внешнего хранилища
    fun deleteExternalFile(filename: String): Boolean {
        return try {
            val file = getExternalFile(filename)
            Log.d(TAG, "Attempting to delete external file: ${file.absolutePath}")

            if (file.exists()) {
                val deleted = file.delete()
                Log.d(TAG, "File deleted: $deleted")
                deleted
            } else {
                Log.w(TAG, "File doesn't exist, cannot delete")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    // Восстановление из резервной копии
    fun restoreFromBackup(filename: String): Boolean {
        return try {
            val backupFile = getInternalBackupFile(filename)
            val externalFile = getExternalFile(filename)

            Log.d(TAG, "Restoring from ${backupFile.absolutePath} to ${externalFile.absolutePath}")

            if (backupFile.exists()) {
                backupFile.copyTo(externalFile, overwrite = true)
                backupFile.delete() // Удаляем резервную копию после восстановления
                val success = externalFile.exists()
                Log.d(TAG, "File restored: $success")
                success
            } else {
                Log.w(TAG, "Backup doesn't exist, cannot restore")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from backup: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    // Удаление резервной копии
    fun deleteBackup(filename: String): Boolean {
        return try {
            val file = getInternalBackupFile(filename)
            Log.d(TAG, "Attempting to delete backup: ${file.absolutePath}")

            if (file.exists()) {
                val deleted = file.delete()
                Log.d(TAG, "Backup deleted: $deleted")
                deleted
            } else {
                Log.w(TAG, "Backup doesn't exist, cannot delete")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getFileDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}