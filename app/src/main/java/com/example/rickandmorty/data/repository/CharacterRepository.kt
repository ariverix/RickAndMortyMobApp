package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.model.CharacterResponse
import com.example.rickandmorty.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CharacterRepository {
    suspend fun getCharacters(page: Int = 1): CharacterResponse? = withContext(Dispatchers.IO) {
        try {
            NetworkModule.api.getCharacters(page)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}