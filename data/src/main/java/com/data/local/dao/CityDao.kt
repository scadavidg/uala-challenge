package com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM cities ORDER BY name ASC, country ASC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE name LIKE '%' || :prefix || '%' ORDER BY LOWER(name) ASC, LOWER(country) ASC")
    suspend fun searchCitiesByPrefix(prefix: String): List<CityEntity>

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCityById(cityId: Int): CityEntity?

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCitiesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCitiesSync(cities: List<CityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)

    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()

    @Transaction
    suspend fun insertCitiesInBatches(cities: List<CityEntity>, batchSize: Int = 1000) {
        cities.chunked(batchSize).forEach { batch ->
            insertCities(batch)
        }
    }

    @Transaction
    fun insertCitiesInBatchesSync(cities: List<CityEntity>, batchSize: Int = 1000) {
        cities.chunked(batchSize).forEach { batch ->
            insertCitiesSync(batch)
        }
    }
}
