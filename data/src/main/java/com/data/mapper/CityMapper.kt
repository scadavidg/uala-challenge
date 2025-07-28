package com.data.mapper

import com.data.dto.CityRemoteDto
import com.domain.models.City
import javax.inject.Inject

class CityMapper @Inject constructor() {

    fun mapToDomain(dto: CityRemoteDto, isFavorite: Boolean = false): City {
        return City(
            id = dto._id,
            name = dto.name,
            country = dto.country,
            lat = dto.coordinates.lat,
            lon = dto.coordinates.lon,
            isFavorite = isFavorite
        )
    }

    fun mapListToDomain(dtos: List<CityRemoteDto>, favoriteIds: Set<Int>): List<City> {
        return dtos.map { dto ->
            mapToDomain(dto, isFavorite = dto._id in favoriteIds)
        }
    }
}
