package com.data.local

interface CityLocalDataSource {
    suspend fun getFavoriteIds(): Set<Int>
    suspend fun isFavorite(cityId: Int): Boolean
    suspend fun addFavorite(cityId: Int)
    suspend fun removeFavorite(cityId: Int)
}