package com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.local.entity.FavoriteCityEntity

@Dao
interface FavoriteCityDao {

    @Query("SELECT * FROM favorite_cities ORDER BY name ASC")
    suspend fun getAllFavoriteCities(): List<FavoriteCityEntity>

    @Query("SELECT * FROM favorite_cities WHERE name LIKE '%' || :prefix || '%' OR country LIKE '%' || :prefix || '%' ORDER BY name ASC")
    suspend fun searchFavoriteCities(prefix: String): List<FavoriteCityEntity>

    @Query("SELECT * FROM favorite_cities WHERE id = :cityId")
    suspend fun getFavoriteCityById(cityId: Int): FavoriteCityEntity?

    @Query("SELECT id FROM favorite_cities")
    suspend fun getFavoriteCityIds(): List<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE id = :cityId)")
    suspend fun isFavorite(cityId: Int): Boolean

    @Query("SELECT COUNT(*) FROM favorite_cities")
    suspend fun getFavoriteCitiesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(favoriteCity: FavoriteCityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCities(favoriteCities: List<FavoriteCityEntity>)

    @Query("DELETE FROM favorite_cities WHERE id = :cityId")
    suspend fun removeFavoriteCity(cityId: Int)

    @Query("DELETE FROM favorite_cities")
    suspend fun deleteAllFavoriteCities()
}
