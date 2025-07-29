package com.data.remote

import com.data.dto.ApiResponseDto
import com.data.dto.ApiSingleCityResponseDto
import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.data.remote.api.CityApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityRemoteDataSourceImplTest {

    private lateinit var dataSource: CityRemoteDataSourceImpl
    private lateinit var mockApiService: CityApiService

    private val testCities = listOf(
        CityRemoteDto(_id = 1, name = "Alabama", country = "US", coordinates = CoordinatesDto(lon = -86.9023, lat = 32.3182)),
        CityRemoteDto(_id = 2, name = "Albuquerque", country = "US", coordinates = CoordinatesDto(lon = -106.6504, lat = 35.0844)),
        CityRemoteDto(_id = 3, name = "Anaheim", country = "US", coordinates = CoordinatesDto(lon = -117.9143, lat = 33.8366))
    )

    @BeforeEach
    fun setup() {
        mockApiService = mockk()
        dataSource = CityRemoteDataSourceImpl(mockApiService)
    }

    @Test
    fun `Given API returns cities successfully, When downloadCities is called, Then should return cities response`() = runTest {
        // Given
        val page = 1
        val limit = 10
        val apiResponse = ApiResponseDto(success = true, data = testCities)
        coEvery { mockApiService.getCities(page = page, limit = limit) } returns apiResponse

        // When
        val result = dataSource.downloadCities(page = page, limit = limit)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(testCities, result.data)
    }

    @Test
    fun `Given API returns empty cities list, When downloadCities is called, Then should return empty response`() = runTest {
        // Given
        val page = 1
        val limit = 10
        val apiResponse = ApiResponseDto(success = true, data = emptyList())
        coEvery { mockApiService.getCities(page = page, limit = limit) } returns apiResponse

        // When
        val result = dataSource.downloadCities(page = page, limit = limit)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `Given API returns failure response, When downloadCities is called, Then should throw NetworkException`() = runTest {
        // Given
        val page = 1
        val limit = 10
        val apiResponse = ApiResponseDto(success = false, data = emptyList())
        coEvery { mockApiService.getCities(page = page, limit = limit) } returns apiResponse

        // When / Then
        try {
            dataSource.downloadCities(page = page, limit = limit)
            fail("Should have thrown NetworkException")
        } catch (e: NetworkException) {
            // Test passes if NetworkException is thrown
        }
    }

    @Test
    fun `Given search parameters are provided, When searchCities is called, Then should return search results`() = runTest {
        // Given
        val prefix = "Al"
        val onlyFavorites = false
        val page = 1
        val limit = 20
        val searchResults = listOf(testCities[0], testCities[1]) // Alabama and Albuquerque
        val apiResponse = ApiResponseDto(success = true, data = searchResults)
        coEvery { mockApiService.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit) } returns apiResponse

        // When
        val result = dataSource.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(2, result.data.size)
        assertEquals("Alabama", result.data[0].name)
        assertEquals("Albuquerque", result.data[1].name)
    }

    @Test
    fun `Given city ID is provided, When getCityById is called, Then should return specific city`() = runTest {
        // Given
        val cityId = 1
        val cityResponse = ApiSingleCityResponseDto(success = true, data = testCities[0])
        coEvery { mockApiService.getCityById(cityId) } returns cityResponse

        // When
        val result = dataSource.getCityById(cityId)

        // Then
        assertNotNull(result)
        assertEquals(cityId, result._id)
        assertEquals("Alabama", result.name)
    }

    @Test
    fun `Given city ID does not exist, When getCityById is called, Then should throw NetworkException`() = runTest {
        // Given
        val cityId = 999
        val cityResponse = ApiSingleCityResponseDto(success = false, data = testCities[0])
        coEvery { mockApiService.getCityById(cityId) } returns cityResponse

        // When / Then
        try {
            dataSource.getCityById(cityId)
            fail("Should have thrown NetworkException")
        } catch (e: NetworkException) {
            // Test passes if NetworkException is thrown
        }
    }

    @Test
    fun `Given search with only favorites filter, When searchCities is called, Then should return only favorite cities`() = runTest {
        // Given
        val prefix = "Al"
        val onlyFavorites = true
        val page = 1
        val limit = 20
        val favoriteResults = listOf(testCities[0]) // Only Alabama as favorite
        val apiResponse = ApiResponseDto(success = true, data = favoriteResults)
        coEvery { mockApiService.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit) } returns apiResponse

        // When
        val result = dataSource.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("Alabama", result.data[0].name)
    }

    @Test
    fun `Given API call is made, When downloadCities is called, Then should call API service with correct parameters`() = runTest {
        // Given
        val page = 2
        val limit = 15
        val apiResponse = ApiResponseDto(success = true, data = testCities)
        coEvery { mockApiService.getCities(page = page, limit = limit) } returns apiResponse

        // When
        dataSource.downloadCities(page = page, limit = limit)

        // Then
        coVerify { mockApiService.getCities(page = page, limit = limit) }
    }

    @Test
    fun `Given API call is made, When searchCities is called, Then should call API service with correct search parameters`() = runTest {
        // Given
        val prefix = "Test"
        val onlyFavorites = false
        val page = 1
        val limit = 20
        val apiResponse = ApiResponseDto(success = true, data = emptyList())
        coEvery { mockApiService.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit) } returns apiResponse

        // When
        dataSource.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit)

        // Then
        coVerify { mockApiService.searchCities(prefix = prefix, onlyFavorites = onlyFavorites, page = page, limit = limit) }
    }
}
