package com.domain.repositories

import com.domain.models.City
import com.domain.models.Result

interface CityRepository {
    /** Returns cities with pagination support. */
    suspend fun getAllCities(page: Int = 1, limit: Int = 20): Result<List<City>>

    /** Returns the cities that match the prefix. */
    suspend fun searchCities(prefix: String, onlyFavorites: Boolean = false): Result<List<City>>

    /** Marks/unmarks a city as favorite. */
    suspend fun toggleFavorite(cityId: Int): Result<Unit>

    /** Returns all favorite cities. */
    suspend fun getFavoriteCities(): Result<List<City>>

    /** Gets a city by ID. */
    suspend fun getCityById(cityId: Int): Result<City?>

    /** Toggles online/offline mode. */
    suspend fun toggleOnlineMode(enabled: Boolean): Result<Unit>

    /** Gets current online mode status. */
    suspend fun isOnlineMode(): Result<Boolean>
}
