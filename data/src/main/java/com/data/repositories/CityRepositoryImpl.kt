package com.data.repositories

import com.data.local.CityLocalDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository
import javax.inject.Inject

class CityRepositoryImpl @Inject constructor(
    private val remoteDataSource: CityRemoteDataSource,
    private val localDataSource: CityLocalDataSource,
    private val mapper: CityMapper
) : CityRepository {

    private var cachedCities: List<City>? = null

    override suspend fun getAllCities(): Result<List<City>> = try {
        if (cachedCities == null) {
            val remoteCities = remoteDataSource.downloadCities()
            val favorites = localDataSource.getFavoriteIds()
            cachedCities = remoteCities.map {
                mapper.mapToDomain(it, it._id in favorites)
            }.sortedBy { it.name.lowercase() + it.country.lowercase() }
        }
        Result.Success(cachedCities ?: emptyList())
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }

    override suspend fun searchCities(prefix: String, onlyFavorites: Boolean): Result<List<City>> {
        return try {
            val normalizedPrefix = prefix.trim().lowercase()

            // If prefix is empty, return all cities (or favorites only)
            if (normalizedPrefix.isEmpty()) {
                val citiesResult = getAllCities()
                val cities = when (citiesResult) {
                    is Result.Success -> citiesResult.data
                    is Result.Error -> emptyList()
                    is Result.Loading -> emptyList()
                }
                val filteredCities = if (onlyFavorites) {
                    cities.filter { it.isFavorite }
                } else {
                    cities
                }
                return Result.Success(filteredCities)
            }

            // Get all cities
            val citiesResult = getAllCities()
            val cities = when (citiesResult) {
                is Result.Success -> citiesResult.data
                is Result.Error -> emptyList()
                is Result.Loading -> emptyList()
            }

            // Use efficient in-memory search instead of pre-built index
            val searchResults = cities.filter { city ->
                city.name.lowercase().startsWith(normalizedPrefix)
            }

            // Apply favorites filter if needed
            val filteredResults = if (onlyFavorites) {
                searchResults.filter { it.isFavorite }
            } else {
                searchResults
            }

            Result.Success(filteredResults)
        } catch (e: Exception) {
            Result.Error(e.message.orEmpty())
        }
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
        val citiesResult = getAllCities()
        val cities = when (citiesResult) {
            is Result.Success -> citiesResult.data
            is Result.Error -> emptyList()
            is Result.Loading -> emptyList()
        }
        val city = cities.find { it.id == cityId }
        Result.Success(city)
    } catch (e: Exception) {
        Result.Error(e.message.orEmpty())
    }
}
