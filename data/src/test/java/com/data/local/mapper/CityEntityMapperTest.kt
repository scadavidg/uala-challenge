package com.data.local.mapper

import com.data.local.entity.CityEntity
import com.domain.models.City
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CityEntityMapperTest {

    @Test
    fun `Given city entity, When mapToDomain is called, Then should return domain city`() {
        // Given
        val entity = CityEntity(
            id = 1,
            name = "Test City",
            country = "TC",
            lat = 10.0,
            lon = 20.0
        )

        // When
        val result = CityEntityMapper.mapToDomain(entity)

        // Then
        assertEquals(1, result.id)
        assertEquals("Test City", result.name)
        assertEquals("TC", result.country)
        assertEquals(10.0, result.lat)
        assertEquals(20.0, result.lon)
        assertFalse(result.isFavorite)
    }

    @Test
    fun `Given city entity with favorite flag, When mapToDomain is called, Then should return domain city with favorite status`() {
        // Given
        val entity = CityEntity(
            id = 1,
            name = "Test City",
            country = "TC",
            lat = 10.0,
            lon = 20.0
        )

        // When
        val result = CityEntityMapper.mapToDomain(entity, isFavorite = true)

        // Then
        assertEquals(1, result.id)
        assertEquals("Test City", result.name)
        assertEquals("TC", result.country)
        assertEquals(10.0, result.lat)
        assertEquals(20.0, result.lon)
        assertTrue(result.isFavorite)
    }

    @Test
    fun `Given domain city, When mapToEntity is called, Then should return city entity`() {
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
        val result = CityEntityMapper.mapToEntity(city)

        // Then
        assertEquals(1, result.id)
        assertEquals("Test City", result.name)
        assertEquals("TC", result.country)
        assertEquals(10.0, result.lat)
        assertEquals(20.0, result.lon)
    }

    @Test
    fun `Given list city entities and favorite ids,When mapToDomainList is called,Then should return domain cities with correct favorite status`() {
        // Given
        val entities = listOf(
            CityEntity(id = 1, name = "City 1", country = "C1", lat = 1.0, lon = 1.0),
            CityEntity(id = 2, name = "City 2", country = "C2", lat = 2.0, lon = 2.0),
            CityEntity(id = 3, name = "City 3", country = "C3", lat = 3.0, lon = 3.0)
        )
        val favoriteIds = setOf(1, 3)

        // When
        val result = CityEntityMapper.mapToDomainList(entities, favoriteIds)

        // Then
        assertEquals(3, result.size)
        assertTrue(result[0].isFavorite) // City 1
        assertFalse(result[1].isFavorite) // City 2
        assertTrue(result[2].isFavorite) // City 3
        assertEquals("City 1", result[0].name)
        assertEquals("City 2", result[1].name)
        assertEquals("City 3", result[2].name)
    }

    @Test
    fun `Given empty list of city entities, When mapToDomainList is called, Then should return empty list`() {
        // Given
        val entities = emptyList<CityEntity>()
        val favoriteIds = setOf<Int>()

        // When
        val result = CityEntityMapper.mapToDomainList(entities, favoriteIds)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given list of city entities with no favorites, When mapToDomainList is called, Then should return domain cities with no favorites`() {
        // Given
        val entities = listOf(
            CityEntity(id = 1, name = "City 1", country = "C1", lat = 1.0, lon = 1.0),
            CityEntity(id = 2, name = "City 2", country = "C2", lat = 2.0, lon = 2.0)
        )
        val favoriteIds = emptySet<Int>()

        // When
        val result = CityEntityMapper.mapToDomainList(entities, favoriteIds)

        // Then
        assertEquals(2, result.size)
        assertFalse(result[0].isFavorite)
        assertFalse(result[1].isFavorite)
    }
}
