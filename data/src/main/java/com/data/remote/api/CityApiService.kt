package com.data.remote.api

import com.data.dto.CityRemoteDto
import retrofit2.http.GET

interface CityApiService {

    @GET("hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json")
    suspend fun getCities(): List<CityRemoteDto>
}
