package com.data.repositories

import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.data.local.AppSettingsDataSource
import com.data.local.CityLocalDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.domain.models.City
import com.domain.models.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityRepositoryImplTest {

    private lateinit var repository: CityRepositoryImpl
    private lateinit var mockRemoteDataSource: CityRemoteDataSource
    private lateinit var mockLocalDataSource: CityLocalDataSource
    private lateinit var mockAppSettingsDataSource: AppSettingsDataSource
    private lateinit var mockMapper: CityMapper

    private val testCities = listOf(
        CityRemoteDto(_id = 1, name = "Alabama", country = "US", coordinates = CoordinatesDto(lon = -86.9023, lat = 32.3182)),
        CityRemoteDto(_id = 2, name = "Albuquerque", country = "US", coordinates = CoordinatesDto(lon = -106.6504, lat = 35.0844)),
        CityRemoteDto(_id = 3, name = "Anaheim", country = "US", coordinates = CoordinatesDto(lon = -117.9143, lat = 33.8366)),
        CityRemoteDto(_id = 4, name = "Arizona", country = "US", coordinates = CoordinatesDto(lon = -111.4312, lat = 33.7298)),
        CityRemoteDto(_id = 5, name = "Sydney", country = "AU", coordinates = CoordinatesDto(lon = 151.2093, lat = -33.8688))
    )

    @BeforeEach
    fun setup() {
        mockRemoteDataSource = mockk()
        mockLocalDataSource = mockk()
        mockAppSettingsDataSource = mockk()
        mockMapper = mockk()

        // Setup app settings to return offline mode by default
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns false

        // Setup mapper to return domain cities
        testCities.forEach { dto ->
            coEvery { mockMapper.mapToDomain(dto, any()) } returns City(
                id = dto._id,
                name = dto.name,
                country = dto.country,
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon,
                isFavorite = false
            )
        }

        // Setup local data source to return empty favorites and test cities
        coEvery { mockLocalDataSource.getFavoriteIds() } returns emptySet()
        coEvery { mockLocalDataSource.getLocalCities() } returns testCities

        repository = CityRepositoryImpl(mockRemoteDataSource, mockLocalDataSource, mockAppSettingsDataSource, mockMapper)
    }

    @Test
    fun searchCities_withPrefixA_shouldReturnAllCitiesStartingWithA() = runTest {
        // When
        val result = repository.searchCities("A", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(4, cities.size)
        assertTrue(cities.all { it.name.startsWith("A", ignoreCase = true) })
        assertTrue(cities.any { it.name == "Alabama" })
        assertTrue(cities.any { it.name == "Albuquerque" })
        assertTrue(cities.any { it.name == "Anaheim" })
        assertTrue(cities.any { it.name == "Arizona" })
    }

    @Test
    fun searchCities_withPrefixS_shouldReturnOnlySydney() = runTest {
        // When
        val result = repository.searchCities("s", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("Sydney", cities.first().name)
    }

    @Test
    fun searchCities_withPrefixAl_shouldReturnAlabamaAndAlbuquerque() = runTest {
        // When
        val result = repository.searchCities("Al", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(2, cities.size)
        assertTrue(cities.any { it.name == "Alabama" })
        assertTrue(cities.any { it.name == "Albuquerque" })
    }

    @Test
    fun searchCities_withPrefixAlb_shouldReturnOnlyAlbuquerque() = runTest {
        // When
        val result = repository.searchCities("Alb", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("Albuquerque", cities.first().name)
    }

    @Test
    fun searchCities_withEmptyPrefix_shouldReturnAllCities() = runTest {
        // When
        val result = repository.searchCities("", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun searchCities_withNonExistentPrefix_shouldReturnEmptyList() = runTest {
        // When
        val result = repository.searchCities("xyz", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(0, cities.size)
    }

    @Test
    fun searchCities_shouldBeCaseInsensitive() = runTest {
        // When
        val result1 = repository.searchCities("al", false)
        val result2 = repository.searchCities("AL", false)
        val result3 = repository.searchCities("Al", false)

        // Then
        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        assertTrue(result3 is Result.Success)

        val cities1 = (result1 as Result.Success).data
        val cities2 = (result2 as Result.Success).data
        val cities3 = (result3 as Result.Success).data

        assertEquals(cities1.size, cities2.size)
        assertEquals(cities1.size, cities3.size)
    }

    @Test
    fun searchCities_withOnlyFavorites_shouldFilterByFavorites() = runTest {
        // Given - Setup some cities as favorites
        val favoriteIds = setOf(1, 3) // Alabama and Anaheim
        coEvery { mockLocalDataSource.getFavoriteIds() } returns favoriteIds

        // Setup mapper to mark some cities as favorites
        coEvery { mockMapper.mapToDomain(testCities[0], true) } returns City(
            id = testCities[0]._id,
            name = testCities[0].name,
            country = testCities[0].country,
            lat = testCities[0].coordinates.lat,
            lon = testCities[0].coordinates.lon,
            isFavorite = true
        )
        coEvery { mockMapper.mapToDomain(testCities[2], true) } returns City(
            id = testCities[2]._id,
            name = testCities[2].name,
            country = testCities[2].country,
            lat = testCities[2].coordinates.lat,
            lon = testCities[2].coordinates.lon,
            isFavorite = true
        )

        // When
        val result = repository.searchCities("A", true)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(2, cities.size)
        assertTrue(cities.all { it.isFavorite })
        assertTrue(cities.any { it.name == "Alabama" })
        assertTrue(cities.any { it.name == "Anaheim" })
    }

    @Test
    fun searchCities_shouldTrimWhitespaceFromPrefix() = runTest {
        // When
        val result = repository.searchCities("  A  ", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(4, cities.size)
        assertTrue(cities.all { it.name.startsWith("A", ignoreCase = true) })
    }

    @Test
    fun searchCities_shouldHandleSpecialCharactersInPrefix() = runTest {
        // Given - Add a city with special characters
        val specialCity = CityRemoteDto(_id = 6, name = "São Paulo", country = "BR", coordinates = CoordinatesDto(lon = -46.6333, lat = -23.5505))
        val allCities = testCities + specialCity

        coEvery { mockLocalDataSource.getLocalCities() } returns allCities
        coEvery { mockMapper.mapToDomain(specialCity, any()) } returns City(
            id = specialCity._id,
            name = specialCity.name,
            country = specialCity.country,
            lat = specialCity.coordinates.lat,
            lon = specialCity.coordinates.lon,
            isFavorite = false
        )

        // When
        val result = repository.searchCities("são", false)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("São Paulo", cities.first().name)
    }

    @Test
    fun getAllCities_offlineMode_shouldReturnLocalCities() = runTest {
        // When
        val result = repository.getAllCities(1, 20)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun getAllCities_onlineMode_shouldReturnRemoteCities() = runTest {
        // Given - Setup online mode
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true
        coEvery { mockRemoteDataSource.downloadCities(page = 1, limit = 20) } returns mockk {
            coEvery { data } returns testCities
        }

        // When
        val result = repository.getAllCities(1, 20)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun toggleFavorite_shouldToggleFavoriteStatus() = runTest {
        // Given
        val cityId = 1
        coEvery { mockLocalDataSource.isFavorite(cityId) } returns false
        coEvery { mockLocalDataSource.addFavorite(cityId) } returns Unit
        coEvery { mockLocalDataSource.removeFavorite(cityId) } returns Unit

        // When
        val result = repository.toggleFavorite(cityId)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun getFavoriteCities_shouldReturnOnlyFavoriteCities() = runTest {
        // Given
        val favoriteIds = setOf(1, 3)
        coEvery { mockLocalDataSource.getFavoriteIds() } returns favoriteIds

        // Setup mapper to mark some cities as favorites
        coEvery { mockMapper.mapToDomain(testCities[0], true) } returns City(
            id = testCities[0]._id,
            name = testCities[0].name,
            country = testCities[0].country,
            lat = testCities[0].coordinates.lat,
            lon = testCities[0].coordinates.lon,
            isFavorite = true
        )
        coEvery { mockMapper.mapToDomain(testCities[2], true) } returns City(
            id = testCities[2]._id,
            name = testCities[2].name,
            country = testCities[2].country,
            lat = testCities[2].coordinates.lat,
            lon = testCities[2].coordinates.lon,
            isFavorite = true
        )

        // When
        val result = repository.getFavoriteCities()

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(2, cities.size)
        assertTrue(cities.all { it.isFavorite })
    }

    @Test
    fun toggleOnlineMode_shouldUpdateOnlineMode() = runTest {
        // Given
        coEvery { mockAppSettingsDataSource.setOnlineMode(any()) } returns Unit

        // When
        val result = repository.toggleOnlineMode(true)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun isOnlineMode_shouldReturnCurrentMode() = runTest {
        // Given
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true

        // When
        val result = repository.isOnlineMode()

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
    }
}
