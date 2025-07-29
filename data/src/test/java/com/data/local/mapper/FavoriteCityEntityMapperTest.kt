package com.data.local.mapper

import com.data.local.entity.FavoriteCityEntity
import com.domain.models.City
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FavoriteCityEntityMapperTest {

    @Test
    fun `Given favorite city entity, When mapToDomain is called, Then should return domain city with favorite status`() {
        // Given
        val entity = FavoriteCityEntity(
            id = 1,
            name = "Favorite City",
            country = "FC",
            lat = 15.0,
            lon = 25.0
        )

        // When
        val result = FavoriteCityEntityMapper.mapToDomain(entity)

        // Then
        assertEquals(1, result.id)
        assertEquals("Favorite City", result.name)
        assertEquals("FC", result.country)
        assertEquals(15.0, result.lat)
        assertEquals(25.0, result.lon)
        assertTrue(result.isFavorite)
    }

    @Test
    fun `Given list of favorite city entities, When mapToDomainList is called, Then should return domain cities with favorite status`() {
        // Given
        val entities = listOf(
            FavoriteCityEntity(id = 1, name = "Favorite 1", country = "F1", lat = 1.0, lon = 1.0),
            FavoriteCityEntity(id = 2, name = "Favorite 2", country = "F2", lat = 2.0, lon = 2.0),
            FavoriteCityEntity(id = 3, name = "Favorite 3", country = "F3", lat = 3.0, lon = 3.0)
        )

        // When
        val result = FavoriteCityEntityMapper.mapToDomainList(entities)

        // Then
        assertEquals(3, result.size)
        assertTrue(result.all { it.isFavorite })
        assertEquals("Favorite 1", result[0].name)
        assertEquals("Favorite 2", result[1].name)
        assertEquals("Favorite 3", result[2].name)
    }

    @Test
    fun `Given empty list of favorite city entities, When mapToDomainList is called, Then should return empty list`() {
        // Given
        val entities = emptyList<FavoriteCityEntity>()

        // When
        val result = FavoriteCityEntityMapper.mapToDomainList(entities)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given domain city, When mapToEntity is called, Then should return favorite city entity`() {
        // Given
        val city = City(
            id = 1,
            name = "Test City",
            country = "TC",
            lat = 10.0,
            lon = 20.0,
            isFavorite = true
        )

        // When
        val result = FavoriteCityEntityMapper.mapToEntity(city)

        // Then
        assertEquals(1, result.id)
        assertEquals("Test City", result.name)
        assertEquals("TC", result.country)
        assertEquals(10.0, result.lat)
        assertEquals(20.0, result.lon)
    }

    @Test
    fun `Given list of domain cities, When mapToEntityList is called, Then should return favorite city entities`() {
        // Given
        val cities = listOf(
            City(id = 1, name = "City 1", country = "C1", lat = 1.0, lon = 1.0, isFavorite = true),
            City(id = 2, name = "City 2", country = "C2", lat = 2.0, lon = 2.0, isFavorite = false),
            City(id = 3, name = "City 3", country = "C3", lat = 3.0, lon = 3.0, isFavorite = true)
        )

        // When
        val result = FavoriteCityEntityMapper.mapToEntityList(cities)

        // Then
        assertEquals(3, result.size)
        assertEquals("City 1", result[0].name)
        assertEquals("City 2", result[1].name)
        assertEquals("City 3", result[2].name)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
        assertEquals(3, result[2].id)
    }

    @Test
    fun `Given empty list of domain cities, When mapToEntityList is called, Then should return empty list`() {
        // Given
        val cities = emptyList<City>()

        // When
        val result = FavoriteCityEntityMapper.mapToEntityList(cities)

        // Then
        assertTrue(result.isEmpty())
    }
}
