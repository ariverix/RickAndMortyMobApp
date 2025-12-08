package com.example.rickandmorty.data.storage

import android.content.Context
import com.example.rickandmorty.ui.home.CharacterUi

object SharedDataManager {
    private val characters = mutableListOf<CharacterUi>()

    fun addCharacters(newCharacters: List<CharacterUi>) {
        characters.addAll(newCharacters)
    }

    fun getAllCharacters(): List<CharacterUi> {
        return characters.toList()
    }

    fun clearCharacters() {
        characters.clear()
    }

    fun getCharactersCount(): Int {
        return characters.size
    }
}