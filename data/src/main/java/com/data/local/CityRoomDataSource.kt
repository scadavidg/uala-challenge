package com.data.local

import com.domain.models.City

interface CityRoomDataSource {
    suspend fun getAllCities(): List<City>
    suspend fun searchCitiesByPrefix(prefix: String): List<City>
    suspend fun getCityById(cityId: Int): City?
    suspend fun getCitiesCount(): Int
    suspend fun insertCities(cities: List<City>)
    suspend fun deleteAllCities()
}
