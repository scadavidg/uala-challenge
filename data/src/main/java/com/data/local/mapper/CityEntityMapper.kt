package com.data.local.mapper

import com.data.local.entity.CityEntity
import com.domain.models.City

object CityEntityMapper {

    fun mapToDomain(entity: CityEntity, isFavorite: Boolean = false): City {
        return City(
            id = entity.id,
            name = entity.name,
            country = entity.country,
            lat = entity.lat,
            lon = entity.lon,
            isFavorite = isFavorite
        )
    }

    fun mapToEntity(city: City): CityEntity {
        return CityEntity(
            id = city.id,
            name = city.name,
            country = city.country,
            lat = city.lat,
            lon = city.lon
        )
    }

    fun mapToDomainList(entities: List<CityEntity>, favoriteIds: Set<Int>): List<City> {
        return entities.map { entity ->
            mapToDomain(entity, entity.id in favoriteIds)
        }
    }
}
