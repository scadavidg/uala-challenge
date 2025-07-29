package com.data.local.mapper

import com.data.local.entity.FavoriteCityEntity
import com.domain.models.City

object FavoriteCityEntityMapper {

    fun mapToDomain(entity: FavoriteCityEntity): City {
        return City(
            id = entity.id,
            name = entity.name,
            country = entity.country,
            lat = entity.lat,
            lon = entity.lon,
            isFavorite = true // Always true since it's a favorite entity
        )
    }

    fun mapToDomainList(entities: List<FavoriteCityEntity>): List<City> {
        return entities.map { mapToDomain(it) }
    }

    fun mapToEntity(city: City): FavoriteCityEntity {
        return FavoriteCityEntity(
            id = city.id,
            name = city.name,
            country = city.country,
            lat = city.lat,
            lon = city.lon
        )
    }

    fun mapToEntityList(cities: List<City>): List<FavoriteCityEntity> {
        return cities.map { mapToEntity(it) }
    }
}
