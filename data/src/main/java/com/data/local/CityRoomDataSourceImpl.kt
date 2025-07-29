package com.data.local

import com.data.local.dao.CityDao
import com.data.local.mapper.CityEntityMapper
import com.domain.models.City
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class CityRoomDataSourceImpl @Inject constructor(
    private val cityDao: CityDao,
    private val favoriteCityRoomDataSource: FavoriteCityRoomDataSource
) : CityRoomDataSource {

    override suspend fun getAllCities(): List<City> {
        val entities = cityDao.getAllCities().first()
        val favoriteIds = favoriteCityRoomDataSource.getFavoriteCityIds().toSet()
        return CityEntityMapper.mapToDomainList(entities, favoriteIds)
    }

    override suspend fun searchCitiesByPrefix(prefix: String): List<City> {
        val entities = cityDao.searchCitiesByPrefix(prefix)
        val favoriteIds = favoriteCityRoomDataSource.getFavoriteCityIds().toSet()
        return CityEntityMapper.mapToDomainList(entities, favoriteIds)
    }

    override suspend fun getCityById(cityId: Int): City? {
        val entity = cityDao.getCityById(cityId) ?: return null
        val isFavorite = favoriteCityRoomDataSource.isFavorite(cityId)
        return CityEntityMapper.mapToDomain(entity, isFavorite)
    }

    override suspend fun getCitiesCount(): Int {
        return cityDao.getCitiesCount()
    }

    override suspend fun insertCities(cities: List<City>) {
        val entities = cities.map { CityEntityMapper.mapToEntity(it) }
        cityDao.insertCities(entities)
    }

    override suspend fun deleteAllCities() {
        cityDao.deleteAllCities()
    }
}
