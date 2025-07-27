package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityRemoteDto(
    @Json(name = "_id")
    val _id: Int,

    @Json(name = "name")
    val name: String,

    @Json(name = "country")
    val country: String,

    @Json(name = "coord")
    val coordinates: CoordinatesDto
)

@JsonClass(generateAdapter = true)
data class CoordinatesDto(
    @Json(name = "lon")
    val lon: Double,

    @Json(name = "lat")
    val lat: Double
)