package com.data.repositories

import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.data.local.CityLocalDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.domain.models.City
import com.domain.models.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityRepositoryImplTest {

    private lateinit var repository: CityRepositoryImpl
    private lateinit var mockRemoteDataSource: CityRemoteDataSource
    private lateinit var mockLocalDataSource: CityLocalDataSource
    private lateinit var mockMapper: CityMapper

    @BeforeEach
    fun setup() {
        mockRemoteDataSource = mockk()
        mockLocalDataSource = mockk()
        mockMapper = mockk()
        repository = CityRepositoryImpl(mockRemoteDataSource, mockLocalDataSource, mockMapper)
    }

    @Test
    fun `getAllCities should return cached cities when available`() = runTest {
        // Given
        val cachedCities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cachedCities)

        // When
        val result = repository.getAllCities()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(cachedCities, (result as Result.Success).data)
    }

    @Test
    fun `getAllCities should download and cache cities when cache is empty`() = runTest {
        // Given
        val remoteCities = listOf(
            CityRemoteDto(1, "Bogotá", "Colombia", CoordinatesDto(4.7110, -74.0721)),
            CityRemoteDto(2, "Medellín", "Colombia", CoordinatesDto(6.2442, -75.5812))
        )
        val favoriteIds = setOf(1)

        coEvery { mockRemoteDataSource.downloadCities() } returns remoteCities
        coEvery { mockLocalDataSource.getFavoriteIds() } returns favoriteIds
        coEvery { mockMapper.mapToDomain(any(), any()) } answers {
            val dto = it.invocation.args[0] as CityRemoteDto
            val isFavorite = it.invocation.args[1] as Boolean
            City(dto._id, dto.name, dto.country, dto.coordinates.lat, dto.coordinates.lon, isFavorite)
        }

        // When
        val result = repository.getAllCities()

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(2, cities.size)
        assertTrue(cities[0].isFavorite)
        assertFalse(cities[1].isFavorite)
    }

    @Test
    fun `searchCities should filter cities by prefix`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false),
            City(3, "Barranquilla", "Colombia", 10.9685, -74.7813, false)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.searchCities("bo", onlyFavorites = false)

        // Then
        assertTrue(result is Result.Success)
        val filteredCities = (result as Result.Success).data
        assertEquals(1, filteredCities.size)
        assertEquals("Bogotá", filteredCities[0].name)
    }

    @Test
    fun `searchCities should filter by prefix and favorites when onlyFavorites is true`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false),
            City(3, "Barranquilla", "Colombia", 10.9685, -74.7813, true)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.searchCities("b", onlyFavorites = true)

        // Then
        assertTrue(result is Result.Success)
        val filteredCities = (result as Result.Success).data
        assertEquals(2, filteredCities.size)
        assertTrue(filteredCities.all { it.isFavorite })
        assertTrue(filteredCities.any { it.name == "Bogotá" })
        assertTrue(filteredCities.any { it.name == "Barranquilla" })
    }

    @Test
    fun `toggleFavorite should add city to favorites when not favorite`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockLocalDataSource.isFavorite(cityId) } returns false
        coEvery { mockLocalDataSource.addFavorite(cityId) } returns Unit

        val cities = listOf(
            City(cityId, "Bogotá", "Colombia", 4.7110, -74.0721, false)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.toggleFavorite(cityId)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `toggleFavorite should remove city from favorites when already favorite`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockLocalDataSource.isFavorite(cityId) } returns true
        coEvery { mockLocalDataSource.removeFavorite(cityId) } returns Unit

        val cities = listOf(
            City(cityId, "Bogotá", "Colombia", 4.7110, -74.0721, true)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.toggleFavorite(cityId)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `getFavoriteCities should return only favorite cities`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false),
            City(3, "Barranquilla", "Colombia", 10.9685, -74.7813, true)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.getFavoriteCities()

        // Then
        assertTrue(result is Result.Success)
        val favoriteCities = (result as Result.Success).data
        assertEquals(2, favoriteCities.size)
        assertTrue(favoriteCities.all { it.isFavorite })
        assertTrue(favoriteCities.any { it.name == "Bogotá" })
        assertTrue(favoriteCities.any { it.name == "Barranquilla" })
    }

    @Test
    fun `getCityById should return city when found`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.getCityById(1)

        // Then
        assertTrue(result is Result.Success)
        val city = (result as Result.Success).data
        assertNotNull(city)
        assertEquals("Bogotá", city?.name)
    }

    @Test
    fun `getCityById should return null when city not found`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true)
        )
        val field = CityRepositoryImpl::class.java.getDeclaredField("cachedCities")
        field.isAccessible = true
        field.set(repository, cities)

        // When
        val result = repository.getCityById(999)

        // Then
        assertTrue(result is Result.Success)
        val city = (result as Result.Success).data
        assertNull(city)
    }
}
