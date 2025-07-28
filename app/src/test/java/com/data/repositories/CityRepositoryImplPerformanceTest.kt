package com.data.repositories

import com.data.local.CityLocalDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.domain.models.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityRepositoryImplPerformanceTest {
    private lateinit var repository: CityRepositoryImpl
    private lateinit var remoteDataSource: CityRemoteDataSource
    private lateinit var localDataSource: CityLocalDataSource
    private lateinit var cityMapper: CityMapper
    private val testDispatcher = StandardTestDispatcher()

    private fun generateLargeCityList(size: Int): List<com.data.dto.CityRemoteDto> = (1..size).map { index ->
        com.data.dto.CityRemoteDto(
            _id = index,
            name = "City $index",
            country = "Country $index",
            coordinates = com.data.dto.CoordinatesDto(
                lon = -74.0 + (index * 0.1),
                lat = 40.0 + (index * 0.1)
            )
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        remoteDataSource = mockk()
        localDataSource = mockk()
        cityMapper = CityMapper()
        repository = CityRepositoryImpl(remoteDataSource, localDataSource, cityMapper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getAllCities_handlesLargeDataset_under1Second() = runTest {
        // Given
        val largeCityList = generateLargeCityList(1000)
        coEvery { remoteDataSource.downloadCities() } returns largeCityList
        coEvery { localDataSource.getFavoriteIds() } returns emptySet()

        // When
        val startTime = System.currentTimeMillis()
        val result = repository.getAllCities()
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Large dataset processing took ${processingTime}ms, expected under 1000ms", processingTime < 1000)
        assertTrue(result is Result.Success)
        assertEquals(largeCityList.size, (result as Result.Success).data.size)
    }

    @Test
    fun getAllCities_handlesMediumDataset_under500ms() = runTest {
        // Given
        val mediumCityList = generateLargeCityList(500)
        coEvery { remoteDataSource.downloadCities() } returns mediumCityList
        coEvery { localDataSource.getFavoriteIds() } returns emptySet()

        // When
        val startTime = System.currentTimeMillis()
        val result = repository.getAllCities()
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Medium dataset processing took ${processingTime}ms, expected under 500ms", processingTime < 500)
        assertTrue(result is Result.Success)
        assertEquals(mediumCityList.size, (result as Result.Success).data.size)
    }

    @Test
    fun getAllCities_memoryEfficient_largeDataset() = runTest {
        // Given
        val largeCityList = generateLargeCityList(2000)
        coEvery { remoteDataSource.downloadCities() } returns largeCityList
        coEvery { localDataSource.getFavoriteIds() } returns emptySet()
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // When
        val result = repository.getAllCities()
        System.gc()
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory
        val memoryUsedMB = memoryUsed / (1024 * 1024)

        // Then
        assertTrue("Memory usage: ${memoryUsedMB}MB, expected under 20MB", memoryUsedMB < 20)
        assertTrue(result is Result.Success)
        assertEquals(largeCityList.size, (result as Result.Success).data.size)
    }

    @Test
    fun getAllCities_sortingPerformance_largeDataset() = runTest {
        // Given
        val largeCityList = generateLargeCityList(1000)
        coEvery { remoteDataSource.downloadCities() } returns largeCityList
        coEvery { localDataSource.getFavoriteIds() } returns emptySet()

        // When
        val startTime = System.currentTimeMillis()
        val result = repository.getAllCities()
        val sortingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Sorting took ${sortingTime}ms, expected under 200ms", sortingTime < 200)
        assertTrue(result is Result.Success)

        val cities = (result as Result.Success).data
        // Verify cities are sorted alphabetically by name + country (as per repository logic)
        for (i in 0 until cities.size - 1) {
            val currentSortKey = "${cities[i].name.lowercase()}${cities[i].country.lowercase()}"
            val nextSortKey = "${cities[i + 1].name.lowercase()}${cities[i + 1].country.lowercase()}"
            assertTrue(
                "Cities not sorted: $currentSortKey should come before $nextSortKey",
                currentSortKey <= nextSortKey
            )
        }
    }

    @Test
    fun getAllCities_errorHandling_under50ms() = runTest {
        // Given
        coEvery { remoteDataSource.downloadCities() } throws Exception("Network error")

        // When
        val startTime = System.currentTimeMillis()
        val result = repository.getAllCities()
        val errorTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Error handling took ${errorTime}ms, expected under 50ms", errorTime < 50)
        assertTrue(result is Result.Error)
        assertEquals("Network error", (result as Result.Error).message)
    }
}
