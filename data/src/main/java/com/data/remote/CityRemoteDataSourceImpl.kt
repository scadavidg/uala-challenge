package com.data.remote

import com.data.dto.CityRemoteDto
import com.data.remote.api.CityApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CityRemoteDataSourceImpl @Inject constructor(
    private val api: CityApiService
) : CityRemoteDataSource {

    override suspend fun downloadCities(): List<CityRemoteDto> {
        return try {
            api.getCities()
        } catch (e: IOException) {
            // Network error like timeout, no internet, etc.
            throw NetworkException("Network error downloading cities", e)
        } catch (e: HttpException) {
            // HTTP error 404, 500, etc.
            throw NetworkException("HTTP error: ${e.code()}", e)
        } catch (e: Exception) {
            // other unknown error
            throw NetworkException("Unexpected error downloading cities", e)
        }
    }
}

class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause)
