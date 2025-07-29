package com.data.local

import com.data.dto.CityRemoteDto
import com.domain.models.City

interface CityLocalDataSource {
    suspend fun getFavoriteIds(): Set<Int>
    suspend fun isFavorite(cityId: Int): Boolean
    suspend fun addFavorite(cityId: Int): Unit
    suspend fun removeFavorite(cityId: Int): Unit
    suspend fun getLocalCities(): List<CityRemoteDto>

    // New methods to handle complete favorite data
    suspend fun addFavoriteCity(city: City): Unit
    suspend fun removeFavoriteCity(cityId: Int): Unit
    suspend fun getFavoriteCitiesData(): List<City>
}
