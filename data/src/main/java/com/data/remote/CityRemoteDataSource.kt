package com.data.remote

import com.data.dto.CityRemoteDto

interface CityRemoteDataSource {
    suspend fun downloadCities(): List<CityRemoteDto>
}
