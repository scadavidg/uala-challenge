package com.data.remote

import com.data.dto.ApiResponseDto
import com.data.dto.CityRemoteDto
import com.data.remote.api.CityApiService
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

class CityRemoteDataSourceImpl @Inject constructor(private val api: CityApiService) : CityRemoteDataSource {

    override suspend fun downloadCities(page: Int, limit: Int): ApiResponseDto = try {
        val response = api.getCities(page = page, limit = limit)

        if (!response.success) {
            throw NetworkException("API returned success=false")
        }

        response
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

    override suspend fun searchCities(prefix: String?, onlyFavorites: Boolean, page: Int, limit: Int): ApiResponseDto = try {
        val response = api.searchCities(
            prefix = prefix,
            onlyFavorites = onlyFavorites,
            page = page,
            limit = limit
        )
        response
    } catch (e: IOException) {
        throw NetworkException("Network error searching cities: ${e.message}", e)
    } catch (e: HttpException) {
        val errorBody = try {
            e.response()?.errorBody()?.string() ?: "Unknown HTTP error"
        } catch (ex: Exception) {
            "Unable to read error body"
        }
        throw NetworkException("HTTP error ${e.code()}: $errorBody", e)
    } catch (e: Exception) {
        // Check if this is a CancellationException (which happens during debouncing)
        if (e is kotlinx.coroutines.CancellationException || e.cause is kotlinx.coroutines.CancellationException) {
            throw e // Re-throw to let the repository handle it properly
        }

        throw NetworkException("Unexpected error searching cities: ${e.message}", e)
    }

    override suspend fun getCityById(id: Int): CityRemoteDto = try {
        val response = api.getCityById(id)

        if (response.success) {
            val city = response.data
            city
        } else {
            throw NetworkException("City not found with ID: $id")
        }
    } catch (e: IOException) {
        throw NetworkException("Network error getting city by ID", e)
    } catch (e: HttpException) {
        throw NetworkException("HTTP error: ${e.code()}", e)
    } catch (e: Exception) {
        throw NetworkException("Unexpected error getting city by ID", e)
    }
}

class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause)
