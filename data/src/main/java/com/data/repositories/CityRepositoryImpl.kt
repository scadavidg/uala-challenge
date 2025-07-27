package com.data.repositories

import com.data.local.CityLocalDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.domain.models.City
import com.domain.repositories.CityRepository
import javax.inject.Inject

class CityRepositoryImpl @Inject constructor(
    private val remoteDataSource: CityRemoteDataSource,
    private val localDataSource: CityLocalDataSource,
    private val mapper: CityMapper
) : CityRepository {

    private var cachedCities: List<City>? = null

    override suspend fun getAllCities(): List<City> {
        if (cachedCities == null) {
            val remoteCities = remoteDataSource.downloadCities()
            val favorites = localDataSource.getFavoriteIds()
            cachedCities = remoteCities.map {
                mapper.mapToDomain(it, it._id in favorites)
            }.sortedBy { it.name.lowercase() + it.country.lowercase() }
        }
        return cachedCities ?: emptyList()
    }

    override suspend fun searchCities(prefix: String, onlyFavorites: Boolean): List<City> {
        val normalizedPrefix = prefix.trim().lowercase()
        val cities = getAllCities()
        return cities.filter { city ->
            val match = city.name.lowercase().startsWith(normalizedPrefix)
            if (onlyFavorites) match && city.isFavorite else match
        }
    }

    override suspend fun toggleFavorite(cityId: Int) {
        val isFavorite = localDataSource.isFavorite(cityId)
        if (isFavorite) {
            localDataSource.removeFavorite(cityId)
        } else {
            localDataSource.addFavorite(cityId)
        }

        cachedCities = cachedCities?.map {
            if (it.id == cityId) it.copy(isFavorite = !isFavorite) else it
        }
    }

    override suspend fun getFavoriteCities(): List<City> {
        return getAllCities().filter { it.isFavorite }
    }

    override suspend fun getCityById(cityId: Int): City? {
        return getAllCities().find { it.id == cityId }
    }
}
