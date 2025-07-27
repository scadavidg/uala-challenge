package com.data.remote

import com.data.dto.CityRemoteDto
import com.data.remote.api.CityApiService
import javax.inject.Inject

class CityRemoteDataSourceImpl @Inject constructor(
    private val api: CityApiService
) : CityRemoteDataSource {

    override suspend fun downloadCities(): List<CityRemoteDto> {
        return try {
            api.getCities()
        } catch (e: Exception) {
            emptyList() // TODO implementar caso de errores en la conexión
        }
    }
}