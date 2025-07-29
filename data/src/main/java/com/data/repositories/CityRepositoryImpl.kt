package com.data.repositories

import com.data.local.AppSettingsDataSource
import com.data.local.CityRoomDataSource
import com.data.local.FavoriteCityRoomDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.data.remote.NetworkException
import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepositoryImpl @Inject constructor(
    private val remoteDataSource: CityRemoteDataSource,
    private val roomDataSource: CityRoomDataSource,
    private val favoriteCityDataSource: FavoriteCityRoomDataSource,
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
            // Always try online first if online mode is enabled
            try {
                val remoteCities = getCitiesFromRemote(page, limit)
                if (remoteCities.isNotEmpty()) {
                    remoteCities
                } else {
                    // If remote returns empty, try Room as fallback
                    try {
                        getCitiesFromRoom()
                    } catch (localException: Exception) {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                // If online fails, try Room as fallback
                try {
                    getCitiesFromRoom()
                } catch (localException: Exception) {
                    emptyList()
                }
            }
        } else {
            // Use Room for offline mode
            getCitiesFromRoom()
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
            searchCitiesOfflineWithRoom(sanitizedPrefix, onlyFavorites)
        }

        Result.Success(sortCitiesAlphabetically(searchResults))
    } catch (e: Exception) {
        handleSearchError(e)
    }

    override suspend fun toggleFavorite(cityId: Int): Result<Unit> {
        return try {
            val isFavorite = favoriteCityDataSource.isFavorite(cityId)

            if (isFavorite) {
                favoriteCityDataSource.removeFavoriteCity(cityId)
            } else {
                // Get the city data to add to favorites
                val city = getCityById(cityId)
                when (city) {
                    is Result.Success -> {
                        city.data?.let { cityData ->
                            favoriteCityDataSource.addFavoriteCity(cityData)
                        }
                    }

                    is Result.Error -> {
                        return Result.Error("Could not add to favorites: ${city.message}")
                    }

                    is Result.Loading -> {
                        return Result.Error("City is still loading")
                    }
                }
            }

            updateCachedCityFavoriteStatus(cityId, !isFavorite)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message.orEmpty())
        }
    }

    override suspend fun getFavoriteCities(): Result<List<City>> = try {
        val favoriteCities = favoriteCityDataSource.getAllFavoriteCities()
        Result.Success(favoriteCities)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun getCityById(cityId: Int): Result<City?> = try {
        updateOnlineModeStatus()

        val city = if (isOnlineMode) {
            getCityFromRemote(cityId)
        } else {
            getCityFromRoom(cityId)
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
        val previousMode = isOnlineMode
        isOnlineMode = appSettingsDataSource.isOnlineMode()
    }

    private suspend fun getCitiesFromRemote(page: Int, limit: Int): List<City> {
        val response = remoteDataSource.downloadCities(page = page, limit = limit)

        val favorites = favoriteCityDataSource.getFavoriteCityIds()

        val cities = response.data.map { dto ->
            val isFavorite = dto._id in favorites
            val city = mapper.mapToDomain(dto, isFavorite)
            city
        }

        return cities
    }

    private suspend fun getCitiesFromRoom(): List<City> {
        val roomCities = roomDataSource.getAllCities()

        // Get favorite IDs to mark cities as favorites
        val favorites = favoriteCityDataSource.getFavoriteCityIds()

        val cities = roomCities.map { city ->
            val isFavorite = city.id in favorites
            val updatedCity = city.copy(isFavorite = isFavorite)
            updatedCity
        }

        return cities
    }

    private suspend fun getCityFromRemote(cityId: Int): City? {
        val cityDto = remoteDataSource.getCityById(cityId)
        val isFavorite = favoriteCityDataSource.isFavorite(cityId)
        return mapper.mapToDomain(cityDto, isFavorite)
    }

    private suspend fun getCityFromRoom(cityId: Int): City? {
        val cityEntity = roomDataSource.getCityById(cityId)
        val isFavorite = favoriteCityDataSource.isFavorite(cityId)
        return cityEntity?.copy(isFavorite = isFavorite)
    }

    private fun sanitizeSearchQuery(prefix: String): String {
        val sanitized = prefix.trim()
        if (sanitized.length > CityRepositoryConstants.MAX_SEARCH_QUERY_LENGTH) {
            throw IllegalArgumentException("Search query too long")
        }
        return sanitized
    }

    private suspend fun searchCitiesOnline(prefix: String, onlyFavorites: Boolean): List<City> = if (onlyFavorites) {
        searchFavoritesOnline(prefix)
    } else {
        searchAllCitiesOnline(prefix)
    }

    private suspend fun searchFavoritesOnline(prefix: String): List<City> {
        val favoriteCities = favoriteCityDataSource.searchFavoriteCities(prefix)
        return favoriteCities
    }

    private suspend fun searchAllCitiesOnline(prefix: String): List<City> {
        val response = remoteDataSource.searchCities(
            prefix = if (prefix.isBlank()) null else prefix,
            onlyFavorites = false,
            page = CityRepositoryConstants.DEFAULT_PAGE,
            limit = CityRepositoryConstants.DEFAULT_SEARCH_LIMIT
        )
        val favorites = favoriteCityDataSource.getFavoriteCityIds()
        return response.data.map { dto ->
            mapper.mapToDomain(dto, dto._id in favorites)
        }
    }

    private suspend fun searchCitiesOfflineWithRoom(prefix: String, onlyFavorites: Boolean): List<City> = if (onlyFavorites) {
        searchFavoritesOfflineWithRoom(prefix)
    } else {
        searchAllCitiesOfflineWithRoom(prefix)
    }

    private suspend fun searchFavoritesOfflineWithRoom(prefix: String): List<City> {
        val favoriteCities = favoriteCityDataSource.searchFavoriteCities(prefix)
        return favoriteCities
    }

    private suspend fun searchAllCitiesOfflineWithRoom(prefix: String): List<City> {
        val roomCities = roomDataSource.searchCitiesByPrefix(prefix)

        // Get favorite IDs to mark cities as favorites
        val favorites = favoriteCityDataSource.getFavoriteCityIds()

        val cities = roomCities.map { city ->
            val isFavorite = city.id in favorites
            city.copy(isFavorite = isFavorite)
        }

        return cities
    }

    private fun sortCitiesAlphabetically(cities: List<City>): List<City> = cities.sortedBy { it.name }

    private fun handleSearchError(e: Exception): Result<List<City>> = when (e) {
        is IllegalArgumentException -> Result.Error(e.message.orEmpty())
        is IOException -> Result.Error("Network error during search")
        is NetworkException -> Result.Error(e.message.orEmpty())
        else -> Result.Error("Unexpected error during search: ${e.message}")
    }

    private fun updateCachedCityFavoriteStatus(cityId: Int, isFavorite: Boolean) {
        cachedCities?.let { cities ->
            val updatedCities = cities.map { city ->
                if (city.id == cityId) {
                    city.copy(isFavorite = isFavorite)
                } else {
                    city
                }
            }
            cachedCities = updatedCities
        }
    }

    private fun clearCache() {
        cachedCities = null
    }
}
