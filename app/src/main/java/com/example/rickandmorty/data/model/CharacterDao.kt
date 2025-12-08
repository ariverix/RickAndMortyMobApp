package com.example.rickandmorty.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    // CREATE - вставка одного или нескольких персонажей
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    // READ - получение всех персонажей (реактивно через Flow)
    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun getAllCharactersFlow(): Flow<List<CharacterEntity>>

    // READ - получение всех персонажей (обычный запрос)
    @Query("SELECT * FROM characters ORDER BY id ASC")
    suspend fun getAllCharacters(): List<CharacterEntity>

    // READ - получение персонажей по странице
    @Query("SELECT * FROM characters WHERE page = :page ORDER BY id ASC")
    suspend fun getCharactersByPage(page: Int): List<CharacterEntity>

    // READ - получение персонажа по ID
    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?

    // READ - проверка есть ли данные в БД
    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCharactersCount(): Int

    // UPDATE - обновление персонажа
    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    // DELETE - удаление персонажа
    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    // DELETE - удаление всех персонажей
    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()

    // DELETE - удаление персонажей по странице
    @Query("DELETE FROM characters WHERE page = :page")
    suspend fun deleteCharactersByPage(page: Int)

    // Получение максимального номера страницы
    @Query("SELECT MAX(page) FROM characters")
    suspend fun getMaxPage(): Int?
}