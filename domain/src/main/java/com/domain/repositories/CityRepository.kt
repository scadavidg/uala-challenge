package com.domain.repositories

import com.domain.models.City

interface CityRepository {
    /** Returns all available cities. */
    suspend fun getAllCities(): List<City>

    /** Returns the cities that match the prefix. */
    suspend fun searchCities(prefix: String, onlyFavorites: Boolean = false): List<City>

    /** Marks/unmarks a city as favorite. */
    suspend fun toggleFavorite(cityId: Int)

    /** Returns all favorite cities. */
    suspend fun getFavoriteCities(): List<City>

    /** Gets a city by ID. */
    suspend fun getCityById(cityId: Int): City?
}