package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponseDto(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "data")
    val data: List<CityRemoteDto>
)

@JsonClass(generateAdapter = true)
data class ApiSingleCityResponseDto(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "data")
    val data: CityRemoteDto
)
