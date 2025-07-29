package com.data.repositories

import com.data.local.AppSettingsDataSource
import com.data.local.CityLocalDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.data.remote.NetworkException
import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository
import java.io.IOException
import javax.inject.Inject

class CityRepositoryImpl @Inject constructor(
    private val remoteDataSource: CityRemoteDataSource,
    private val localDataSource: CityLocalDataSource,
    private val appSettingsDataSource: AppSettingsDataSource,
    private val mapper: CityMapper
) : CityRepository {

    private var cachedCities: List<City>? = null
    private var isOnlineMode: Boolean = false

    override suspend fun getAllCities(page: Int, limit: Int): Result<List<City>> = try {
        isOnlineMode = appSettingsDataSource.isOnlineMode()

        if (isOnlineMode) {
            // Online mode - use API with pagination
            val response = remoteDataSource.downloadCities(page = page, limit = limit)
            val favorites = localDataSource.getFavoriteIds()
            val cities = response.data.map {
                mapper.mapToDomain(it, it._id in favorites)
            }

            cachedCities = cities
            Result.Success(cities)
        } else {
            // Offline mode - use local JSON file as-is (no sorting)
            val localCities = localDataSource.getLocalCities()
            val favorites = localDataSource.getFavoriteIds()
            val cities = localCities.map {
                mapper.mapToDomain(it, it._id in favorites)
            }

            cachedCities = cities
            Result.Success(cities)
        }
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    /**
     * Sorts cities alphabetically by city name first, then by country.
     * This follows the requirement: "Denver, US" should appear before "Sydney, Australia"
     */
    private fun sortCitiesAlphabetically(cities: List<City>): List<City> = cities.sortedWith(
        compareBy<City> { it.name.lowercase() }
            .thenBy { it.country.lowercase() }
    )

    override suspend fun searchCities(prefix: String, onlyFavorites: Boolean): Result<List<City>> = try {
        isOnlineMode = appSettingsDataSource.isOnlineMode()

        // Validate and sanitize the prefix
        val sanitizedPrefix = prefix.trim()
        if (sanitizedPrefix.length > 50) {
            Result.Error("Search query too long")
        }

        if (isOnlineMode) {
            // Online mode - use API search
            val response = remoteDataSource.searchCities(
                prefix = if (sanitizedPrefix.isBlank()) null else sanitizedPrefix,
                onlyFavorites = onlyFavorites,
                page = 1,
                limit = 100
            )
            val favorites = localDataSource.getFavoriteIds()
            val cities = response.data.map {
                mapper.mapToDomain(it, it._id in favorites)
            }
            // Sort results alphabetically as API might not guarantee order
            val sortedCities = sortCitiesAlphabetically(cities)
            Result.Success(sortedCities)
        } else {
            // Offline mode - local search
            val citiesResult = getAllCities()
            val cities = when (citiesResult) {
                is Result.Success -> citiesResult.data
                is Result.Error -> {
                    return Result.Error("Failed to load cities for search: ${citiesResult.message}")
                }

                is Result.Loading -> {
                    return Result.Error("Cities are still loading, please try again")
                }
            }

            val normalizedPrefix = sanitizedPrefix.lowercase()
            val searchResults = if (normalizedPrefix.isEmpty()) {
                cities
            } else {
                cities.filter { city ->
                    city.name.lowercase().startsWith(normalizedPrefix)
                }
            }

            val filteredResults = if (onlyFavorites) {
                searchResults.filter { it.isFavorite }
            } else {
                searchResults
            }

            // Sort results alphabetically for consistent display
            val sortedResults = sortCitiesAlphabetically(filteredResults)
            Result.Success(sortedResults)
        }
    } catch (e: Exception) {
        // Check if this is a CancellationException (which happens during debouncing)
        if (e is kotlinx.coroutines.CancellationException || e.cause is kotlinx.coroutines.CancellationException) {
            throw e // Re-throw to let the ViewModel handle it properly
        }

        val errorMessage = when (e) {
            is NetworkException -> "Network error: ${e.message}"
            is IOException -> "Connection error: ${e.message}"
            else -> "Search error: ${e.message}"
        }
        Result.Error(errorMessage)
    }

    override suspend fun toggleFavorite(cityId: Int): Result<Unit> = try {
        val isFavorite = localDataSource.isFavorite(cityId)
        if (isFavorite) {
            localDataSource.removeFavorite(cityId)
        } else {
            localDataSource.addFavorite(cityId)
        }

        cachedCities = cachedCities?.map {
            if (it.id == cityId) it.copy(isFavorite = !isFavorite) else it
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun getFavoriteCities(): Result<List<City>> = try {
        val citiesResult = getAllCities()
        val cities = when (citiesResult) {
            is Result.Success -> citiesResult.data
            is Result.Error -> emptyList()
            is Result.Loading -> emptyList()
        }
        val favoriteCities = cities.filter { it.isFavorite }
        Result.Success(favoriteCities)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun getCityById(cityId: Int): Result<City?> = try {
        isOnlineMode = appSettingsDataSource.isOnlineMode()

        if (isOnlineMode) {
            // Online mode - use API
            try {
                val cityDto = remoteDataSource.getCityById(cityId)

                val isFavorite = localDataSource.isFavorite(cityId)

                val city = mapper.mapToDomain(cityDto, isFavorite)

                Result.Success(city)
            } catch (e: Exception) {
                throw e
            }
        } else {
            // Offline mode - search in local data
            val localCities = localDataSource.getLocalCities()
            val favorites = localDataSource.getFavoriteIds()
            val cityDto = localCities.find { it._id == cityId }
            val city = cityDto?.let { mapper.mapToDomain(it, it._id in favorites) }
            Result.Success(city)
        }
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun toggleOnlineMode(enabled: Boolean): Result<Unit> = try {
        appSettingsDataSource.setOnlineMode(enabled)
        // Clear cache when switching modes to ensure fresh data
        cachedCities = null
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun isOnlineMode(): Result<Boolean> = try {
        val isOnline = appSettingsDataSource.isOnlineMode()
        Result.Success(isOnline)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }
}
