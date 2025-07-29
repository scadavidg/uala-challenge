package com.data.remote.api

import com.data.dto.ApiResponseDto
import com.data.dto.ApiSingleCityResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CityApiService {
    @GET("api/cities")
    suspend fun getCities(@Query("page") page: Int = 1, @Query("limit") limit: Int = 20): ApiResponseDto

    @GET("api/cities/search")
    suspend fun searchCities(
        @Query("prefix") prefix: String? = null,
        @Query("onlyFavorites") onlyFavorites: Boolean = false,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponseDto

    @GET("api/cities/{id}")
    suspend fun getCityById(@Path("id") id: Int): ApiSingleCityResponseDto
}
