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

    // Optimized search index for fast prefix searches
    // This preprocesses cities into a more efficient structure for prefix matching
    private var searchIndex: Map<String, List<City>>? = null

    override suspend fun getAllCities(): Result<List<City>> {
        return try {
            if (cachedCities == null) {
                val remoteCities = remoteDataSource.downloadCities()
                val favorites = localDataSource.getFavoriteIds()
                cachedCities = remoteCities.map {
                    mapper.mapToDomain(it, it._id in favorites)
                }.sortedBy { it.name.lowercase() + it.country.lowercase() }

                // Build search index for fast prefix searches
                buildSearchIndex()
            }
            Result.Success(cachedCities ?: emptyList())
        } catch (e: Exception) {
            Result.Error(e.message.orEmpty())
        }
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

            // Ensure search index is built
            if (searchIndex == null) {
                getAllCities()
            }

            // Use optimized search index for prefix matching
            val searchResults = searchIndex?.get(normalizedPrefix) ?: emptyList()

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

    override suspend fun toggleFavorite(cityId: Int): Result<Unit> {
        return try {
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
    }

    override suspend fun getFavoriteCities(): Result<List<City>> {
        return try {
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
    }

    override suspend fun getCityById(cityId: Int): Result<City?> {
        return try {
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

    /**
     * Builds an optimized search index for fast prefix matching.
     * This preprocesses cities into a map where keys are lowercase prefixes
     * and values are lists of cities that start with that prefix.
     *
     * Time complexity: O(n * m) where n is number of cities and m is average city name length
     * Space complexity: O(n * m) for storing all possible prefixes
     *
     * This optimization provides O(1) lookup time for prefix searches,
     * making the search extremely fast even with large datasets.
     */
    private fun buildSearchIndex() {
        val cities = cachedCities ?: return
        val index = mutableMapOf<String, MutableList<City>>()

        cities.forEach { city ->
            val cityName = city.name.lowercase()

            // Generate all possible prefixes for this city name
            for (i in 1..cityName.length) {
                val prefix = cityName.substring(0, i)
                index.getOrPut(prefix) { mutableListOf() }.add(city)
            }
        }

        searchIndex = index
    }
}
