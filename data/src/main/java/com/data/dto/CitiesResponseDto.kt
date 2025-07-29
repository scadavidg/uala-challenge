package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CitiesResponseDto(
    @Json(name = "cities")
    val cities: List<CityRemoteDto>,

    @Json(name = "total")
    val total: Int,

    @Json(name = "page")
    val page: Int,

    @Json(name = "limit")
    val limit: Int,

    @Json(name = "totalPages")
    val totalPages: Int
)
