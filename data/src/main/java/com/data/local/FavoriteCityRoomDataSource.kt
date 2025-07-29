package com.data.local

import com.data.local.dao.FavoriteCityDao
import com.data.local.mapper.FavoriteCityEntityMapper
import com.domain.models.City
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteCityRoomDataSource @Inject constructor(
    private val favoriteCityDao: FavoriteCityDao
) {

    suspend fun getAllFavoriteCities(): List<City> {
        val entities = favoriteCityDao.getAllFavoriteCities()
        return FavoriteCityEntityMapper.mapToDomainList(entities)
    }

    suspend fun searchFavoriteCities(prefix: String): List<City> {
        val entities = favoriteCityDao.searchFavoriteCities(prefix)
        return FavoriteCityEntityMapper.mapToDomainList(entities)
    }

    suspend fun getFavoriteCityById(cityId: Int): City? {
        val entity = favoriteCityDao.getFavoriteCityById(cityId)
        return entity?.let { FavoriteCityEntityMapper.mapToDomain(it) }
    }

    suspend fun getFavoriteCityIds(): List<Int> {
        return favoriteCityDao.getFavoriteCityIds()
    }

    suspend fun isFavorite(cityId: Int): Boolean {
        return favoriteCityDao.isFavorite(cityId)
    }

    suspend fun getFavoriteCitiesCount(): Int {
        return favoriteCityDao.getFavoriteCitiesCount()
    }

    suspend fun addFavoriteCity(city: City) {
        val entity = FavoriteCityEntityMapper.mapToEntity(city)
        favoriteCityDao.insertFavoriteCity(entity)
    }

    suspend fun addFavoriteCities(cities: List<City>) {
        val entities = FavoriteCityEntityMapper.mapToEntityList(cities)
        favoriteCityDao.insertFavoriteCities(entities)
    }

    suspend fun removeFavoriteCity(cityId: Int) {
        favoriteCityDao.removeFavoriteCity(cityId)
    }

    suspend fun deleteAllFavoriteCities() {
        favoriteCityDao.deleteAllFavoriteCities()
    }
}
