package com.data.remote

import com.data.dto.ApiResponseDto
import com.data.dto.CityRemoteDto

interface CityRemoteDataSource {
    suspend fun downloadCities(page: Int = 1, limit: Int = 20): ApiResponseDto
    suspend fun searchCities(prefix: String? = null, onlyFavorites: Boolean = false, page: Int = 1, limit: Int = 20): ApiResponseDto

    suspend fun getCityById(id: Int): CityRemoteDto
}
