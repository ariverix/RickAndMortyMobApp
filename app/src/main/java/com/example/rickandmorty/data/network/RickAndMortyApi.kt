package com.example.rickandmorty.data.network

import com.example.rickandmorty.data.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterResponse
}
