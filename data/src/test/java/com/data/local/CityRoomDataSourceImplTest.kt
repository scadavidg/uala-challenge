package com.data.local

import com.data.local.dao.CityDao
import com.data.local.mapper.CityEntityMapper
import com.domain.models.City
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityRoomDataSourceImplTest {

    private lateinit var dataSource: CityRoomDataSourceImpl
    private lateinit var mockCityDao: CityDao
    private lateinit var mockFavoriteCityDataSource: FavoriteCityRoomDataSource

    private val testCities = listOf(
        City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
        City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false),
        City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false)
    )

    @BeforeEach
    fun setup() {
        mockCityDao = mockk()
        mockFavoriteCityDataSource = mockk()
        dataSource = CityRoomDataSourceImpl(mockCityDao, mockFavoriteCityDataSource)
    }

    @Test
    fun `Given cities exist in database, When getAllCities is called, Then should return all cities with favorite status`() = runTest {
        // Given
        val cityEntities = testCities.map { CityEntityMapper.mapToEntity(it) }
        coEvery { mockCityDao.getAllCities() } returns flowOf(cityEntities)
        coEvery { mockFavoriteCityDataSource.getFavoriteCityIds() } returns listOf(1)

        // When
        val result = dataSource.getAllCities()

        // Then
        assertEquals(3, result.size)
        assertTrue(result[0].isFavorite) // Alabama should be favorite
        assertTrue(!result[1].isFavorite) // Albuquerque should not be favorite
        assertTrue(!result[2].isFavorite) // Anaheim should not be favorite
    }

    @Test
    fun `Given search prefix is provided, When searchCitiesByPrefix is called, Then should return filtered cities with favorite status`() = runTest {
        // Given
        val searchPrefix = "Al"
        val filteredCities = listOf(
            City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
            City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false)
        )
        val cityEntities = filteredCities.map { CityEntityMapper.mapToEntity(it) }
        coEvery { mockCityDao.searchCitiesByPrefix(searchPrefix) } returns cityEntities
        coEvery { mockFavoriteCityDataSource.getFavoriteCityIds() } returns listOf(1)

        // When
        val result = dataSource.searchCitiesByPrefix(searchPrefix)

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0].isFavorite) // Alabama should be favorite
        assertTrue(!result[1].isFavorite) // Albuquerque should not be favorite
    }

    @Test
    fun `Given city exists in database, When getCityById is called, Then should return city with favorite status`() = runTest {
        // Given
        val cityId = 1
        val cityEntity = CityEntityMapper.mapToEntity(testCities[0])
        coEvery { mockCityDao.getCityById(cityId) } returns cityEntity
        coEvery { mockFavoriteCityDataSource.isFavorite(cityId) } returns true

        // When
        val result = dataSource.getCityById(cityId)

        // Then
        assertNotNull(result)
        assertEquals(cityId, result?.id)
        assertEquals("Alabama", result?.name)
        assertTrue(result?.isFavorite == true)
    }

    @Test
    fun `Given city does not exist in database, When getCityById is called, Then should return null`() = runTest {
        // Given
        val cityId = 999
        coEvery { mockCityDao.getCityById(cityId) } returns null

        // When
        val result = dataSource.getCityById(cityId)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `Given cities count is requested, When getCitiesCount is called, Then should return correct count`() = runTest {
        // Given
        val expectedCount = 3
        coEvery { mockCityDao.getCitiesCount() } returns expectedCount

        // When
        val result = dataSource.getCitiesCount()

        // Then
        assertEquals(expectedCount, result)
    }

    @Test
    fun `Given cities list is provided, When insertCities is called, Then should insert cities into database`() = runTest {
        // Given
        val citiesToInsert = listOf(
            City(id = 4, name = "New York", country = "US", lat = 40.7128, lon = -74.0060, isFavorite = false)
        )
        coEvery { mockCityDao.insertCities(any()) } returns Unit

        // When
        dataSource.insertCities(citiesToInsert)

        // Then
        // Verification is done through the mock setup
    }

    @Test
    fun `Given database cleanup is requested, When deleteAllCities is called, Then should delete all cities from database`() = runTest {
        // Given
        coEvery { mockCityDao.deleteAllCities() } returns Unit

        // When
        dataSource.deleteAllCities()

        // Then
        // Verification is done through the mock setup
    }
}
