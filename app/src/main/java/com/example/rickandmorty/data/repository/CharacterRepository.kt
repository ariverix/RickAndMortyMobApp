package com.example.rickandmorty.data.repository

import android.content.Context
import com.example.rickandmorty.data.db.AppDatabase
import com.example.rickandmorty.data.db.CharacterEntity
import com.example.rickandmorty.data.mapper.CharacterMapper
import com.example.rickandmorty.data.network.NetworkModule
import com.example.rickandmorty.ui.home.CharacterUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CharacterRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val characterDao = database.characterDao()

    // FLOW для реактивного обновления UI
    val charactersFlow: Flow<List<CharacterUi>> = characterDao
        .getAllCharactersFlow()
        .map { entities -> CharacterMapper.entitiesToUi(entities) }

    // Проверка наличия данных в БД
    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        characterDao.getCharactersCount() == 0
    }

    // Получение всех персонажей из БД
    suspend fun getCharactersFromDb(): List<CharacterUi> = withContext(Dispatchers.IO) {
        val entities = characterDao.getAllCharacters()
        CharacterMapper.entitiesToUi(entities)
    }

    // Загрузка персонажей из API и сохранение в БД
    suspend fun fetchAndSaveCharacters(page: Int): Result<List<CharacterUi>> =
        withContext(Dispatchers.IO) {
            try {
                val response = NetworkModule.api.getCharacters(page)

                // Конвертируем DTO в Entity
                val entities = CharacterMapper.dtosToEntities(response.results, page)

                // Сохраняем в БД
                characterDao.insertCharacters(entities)

                // Возвращаем UI модели
                val uiModels = CharacterMapper.dtosToUi(response.results)
                Result.success(uiModels)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    // Обновление списка персонажей (удаление старых и загрузка новых)
    suspend fun refreshCharacters(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Удаляем все старые данные
            characterDao.deleteAllCharacters()

            // Загружаем первую страницу
            val response = NetworkModule.api.getCharacters(1)
            val entities = CharacterMapper.dtosToEntities(response.results, 1)
            characterDao.insertCharacters(entities)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Загрузка следующей страницы
    suspend fun loadNextPage(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val currentMaxPage = characterDao.getMaxPage() ?: 0
            val nextPage = currentMaxPage + 1

            val response = NetworkModule.api.getCharacters(nextPage)
            val entities = CharacterMapper.dtosToEntities(response.results, nextPage)
            characterDao.insertCharacters(entities)

            Result.success(nextPage)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Получение персонажа по ID
    suspend fun getCharacterById(id: Int): CharacterEntity? = withContext(Dispatchers.IO) {
        characterDao.getCharacterById(id)
    }

    // Обновление персонажа
    suspend fun updateCharacter(character: CharacterEntity) = withContext(Dispatchers.IO) {
        characterDao.updateCharacter(character)
    }

    // Удаление персонажа
    suspend fun deleteCharacter(character: CharacterEntity) = withContext(Dispatchers.IO) {
        characterDao.deleteCharacter(character)
    }

    // Удаление всех персонажей
    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        characterDao.deleteAllCharacters()
    }
}