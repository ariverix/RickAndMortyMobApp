package com.example.rickandmorty.data.mapper

import com.example.rickandmorty.data.db.CharacterEntity
import com.example.rickandmorty.data.model.CharacterDto
import com.example.rickandmorty.ui.home.CharacterUi

object CharacterMapper {

    // DTO -> Entity
    fun dtoToEntity(dto: CharacterDto, page: Int): CharacterEntity {
        return CharacterEntity(
            id = dto.id,
            name = dto.name,
            status = dto.status,
            species = dto.species,
            gender = dto.gender,
            image = dto.image,
            page = page
        )
    }

    // Entity -> UI
    fun entityToUi(entity: CharacterEntity): CharacterUi {
        return CharacterUi(
            name = entity.name,
            image = entity.image,
            status = entity.status,
            species = entity.species
        )
    }

    // DTO -> UI
    fun dtoToUi(dto: CharacterDto): CharacterUi {
        return CharacterUi(
            name = dto.name,
            image = dto.image,
            status = dto.status,
            species = dto.species
        )
    }

    // List conversions
    fun dtosToEntities(dtos: List<CharacterDto>, page: Int): List<CharacterEntity> {
        return dtos.map { dtoToEntity(it, page) }
    }

    fun entitiesToUi(entities: List<CharacterEntity>): List<CharacterUi> {
        return entities.map { entityToUi(it) }
    }

    fun dtosToUi(dtos: List<CharacterDto>): List<CharacterUi> {
        return dtos.map { dtoToUi(it) }
    }
}