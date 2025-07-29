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

    companion object {
        // Constants moved to CityRepositoryConstants.kt
    }

    override suspend fun getAllCities(page: Int, limit: Int): Result<List<City>> = try {
        updateOnlineModeStatus()
        
        val cities = if (isOnlineMode) {
            getCitiesFromRemote(page, limit)
        } else {
            getCitiesFromLocal()
        }
        
        cachedCities = cities
        Result.Success(cities)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun searchCities(prefix: String, onlyFavorites: Boolean): Result<List<City>> = try {
        updateOnlineModeStatus()
        
        val sanitizedPrefix = sanitizeSearchQuery(prefix)
        
        val searchResults = if (isOnlineMode) {
            searchCitiesOnline(sanitizedPrefix, onlyFavorites)
        } else {
            searchCitiesOffline(sanitizedPrefix, onlyFavorites)
        }
        
        Result.Success(sortCitiesAlphabetically(searchResults))
    } catch (e: Exception) {
        handleSearchError(e)
    }

    override suspend fun toggleFavorite(cityId: Int): Result<Unit> = try {
        val isFavorite = localDataSource.isFavorite(cityId)
        
        if (isFavorite) {
            localDataSource.removeFavoriteCity(cityId)
        } else {
            localDataSource.addFavorite(cityId)
        }
        
        updateCachedCityFavoriteStatus(cityId, !isFavorite)
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
        
        val favoriteCitiesFromList = cities.filter { it.isFavorite }
        
        if (favoriteCitiesFromList.isNotEmpty()) {
            Result.Success(favoriteCitiesFromList)
        } else {
            // Fallback: use the new method that gets complete favorite data
            val favoriteCities = localDataSource.getFavoriteCitiesData()
            Result.Success(favoriteCities)
        }
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun getCityById(cityId: Int): Result<City?> = try {
        updateOnlineModeStatus()
        
        val city = if (isOnlineMode) {
            getCityFromRemote(cityId)
        } else {
            getCityFromLocal(cityId)
        }
        
        Result.Success(city)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun toggleOnlineMode(enabled: Boolean): Result<Unit> = try {
        appSettingsDataSource.setOnlineMode(enabled)
        clearCache()
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

    // MARK: - Private Helper Methods

    private suspend fun updateOnlineModeStatus() {
        isOnlineMode = appSettingsDataSource.isOnlineMode()
    }

    private suspend fun getCitiesFromRemote(page: Int, limit: Int): List<City> {
        val response = remoteDataSource.downloadCities(page = page, limit = limit)
        val favorites = localDataSource.getFavoriteIds()
        return response.data.map { dto ->
            mapper.mapToDomain(dto, dto._id in favorites)
        }
    }

    private suspend fun getCitiesFromLocal(): List<City> {
        val localCities = localDataSource.getLocalCities()
        val favorites = localDataSource.getFavoriteIds()
        return localCities.map { dto ->
            mapper.mapToDomain(dto, dto._id in favorites)
        }
    }

    private fun sanitizeSearchQuery(prefix: String): String {
        val sanitized = prefix.trim()
        if (sanitized.length > CityRepositoryConstants.MAX_SEARCH_QUERY_LENGTH) {
            throw IllegalArgumentException("Search query too long")
        }
        return sanitized
    }

    private suspend fun searchCitiesOnline(prefix: String, onlyFavorites: Boolean): List<City> {
        return if (onlyFavorites) {
            searchFavoritesOnline(prefix)
        } else {
            searchAllCitiesOnline(prefix)
        }
    }

    private suspend fun searchFavoritesOnline(prefix: String): List<City> {
        val favoriteCitiesResult = getFavoriteCities()
        return when (favoriteCitiesResult) {
            is Result.Success -> {
                val favoriteCities = favoriteCitiesResult.data
                filterCitiesByPrefix(favoriteCities, prefix)
            }
            is Result.Error -> {
                throw Exception("Failed to load favorite cities for search: ${favoriteCitiesResult.message}")
            }
            is Result.Loading -> {
                throw Exception("Favorite cities are still loading, please try again")
            }
        }
    }

    private suspend fun searchAllCitiesOnline(prefix: String): List<City> {
        val response = remoteDataSource.searchCities(
            prefix = if (prefix.isBlank()) null else prefix,
            onlyFavorites = false,
            page = CityRepositoryConstants.DEFAULT_PAGE,
            limit = CityRepositoryConstants.DEFAULT_SEARCH_LIMIT
        )
        val favorites = localDataSource.getFavoriteIds()
        return response.data.map { dto ->
            mapper.mapToDomain(dto, dto._id in favorites)
        }
    }

    private suspend fun searchCitiesOffline(prefix: String, onlyFavorites: Boolean): List<City> {
        val citiesToSearch = if (onlyFavorites) {
            getCitiesForOfflineFavoritesSearch()
        } else {
            getCitiesForOfflineAllSearch()
        }
        
        return filterCitiesByPrefix(citiesToSearch, prefix)
    }

    private suspend fun getCitiesForOfflineFavoritesSearch(): List<City> {
        val favoriteCitiesResult = getFavoriteCities()
        return when (favoriteCitiesResult) {
            is Result.Success -> favoriteCitiesResult.data
            is Result.Error -> {
                throw Exception("Failed to load favorite cities for search: ${favoriteCitiesResult.message}")
            }
            is Result.Loading -> {
                throw Exception("Favorite cities are still loading, please try again")
            }
        }
    }

    private suspend fun getCitiesForOfflineAllSearch(): List<City> {
        val citiesResult = getAllCities()
        return when (citiesResult) {
            is Result.Success -> citiesResult.data
            is Result.Error -> {
                throw Exception("Failed to load cities for search: ${citiesResult.message}")
            }
            is Result.Loading -> {
                throw Exception("Cities are still loading, please try again")
            }
        }
    }

    private fun filterCitiesByPrefix(cities: List<City>, prefix: String): List<City> {
        val normalizedPrefix = prefix.lowercase()
        return if (normalizedPrefix.isEmpty()) {
            cities
        } else {
            cities.filter { city ->
                city.name.lowercase().startsWith(normalizedPrefix)
            }
        }
    }

    private suspend fun getCityFromRemote(cityId: Int): City? {
        val cityDto = remoteDataSource.getCityById(cityId)
        val isFavorite = localDataSource.isFavorite(cityId)
        return mapper.mapToDomain(cityDto, isFavorite)
    }

    private suspend fun getCityFromLocal(cityId: Int): City? {
        val localCities = localDataSource.getLocalCities()
        val favorites = localDataSource.getFavoriteIds()
        val cityDto = localCities.find { it._id == cityId }
        return cityDto?.let { mapper.mapToDomain(it, it._id in favorites) }
    }

    private fun updateCachedCityFavoriteStatus(cityId: Int, isFavorite: Boolean) {
        cachedCities = cachedCities?.map { city ->
            if (city.id == cityId) city.copy(isFavorite = isFavorite) else city
        }
    }

    private fun clearCache() {
        cachedCities = null
    }

    private fun handleSearchError(e: Exception): Result<List<City>> {
        // Check if this is a CancellationException (which happens during debouncing)
        if (e is kotlinx.coroutines.CancellationException || e.cause is kotlinx.coroutines.CancellationException) {
            throw e // Re-throw to let the ViewModel handle it properly
        }

        val errorMessage = when (e) {
            is NetworkException -> "Network error: ${e.message}"
            is IOException -> "Connection error: ${e.message}"
            is IllegalArgumentException -> e.message ?: "Invalid search query"
            else -> "Search error: ${e.message}"
        }
        return Result.Error(errorMessage)
    }

    /**
     * Sorts cities alphabetically by city name first, then by country.
     * This follows the requirement: "Denver, US" should appear before "Sydney, Australia"
     */
    private fun sortCitiesAlphabetically(cities: List<City>): List<City> = cities.sortedWith(
        compareBy<City> { it.name.lowercase() }
            .thenBy { it.country.lowercase() }
    )
}
