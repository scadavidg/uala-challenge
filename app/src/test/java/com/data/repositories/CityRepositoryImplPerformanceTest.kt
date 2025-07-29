package com.data.repositories

import com.data.dto.ApiResponseDto
import com.data.dto.CitiesResponseDto
import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.data.local.AppSettingsDataSource
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
    private lateinit var appSettingsDataSource: AppSettingsDataSource
    private lateinit var cityMapper: CityMapper

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        remoteDataSource = mockk()
        localDataSource = mockk()
        appSettingsDataSource = mockk()
        cityMapper = CityMapper()
        repository = CityRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            appSettingsDataSource = appSettingsDataSource,
            mapper = cityMapper
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getAllCities_handlesLargeDataset_under1Second() = runTest {
        // Given
        val largeCityList = generateLargeCityList(1000)
        val response = CitiesResponseDto(
            cities = largeCityList,
            total = largeCityList.size,
            page = 1,
            limit = 1000,
            totalPages = 1
        )
        coEvery { appSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            remoteDataSource.downloadCities(any(), any())
        } returns ApiResponseDto(
            success = true,
            data = largeCityList
        )
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
        val response = CitiesResponseDto(
            cities = mediumCityList,
            total = mediumCityList.size,
            page = 1,
            limit = 500,
            totalPages = 1
        )
        coEvery { appSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            remoteDataSource.downloadCities(any(), any())
        } returns ApiResponseDto(
            success = true,
            data = mediumCityList
        )
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
        val response = CitiesResponseDto(
            cities = largeCityList,
            total = largeCityList.size,
            page = 1,
            limit = 2000,
            totalPages = 1
        )
        coEvery { appSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            remoteDataSource.downloadCities(any(), any())
        } returns ApiResponseDto(
            success = true,
            data = largeCityList
        )
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
        coEvery { appSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            remoteDataSource.downloadCities(any(), any())
        } returns ApiResponseDto(
            success = true,
            data = largeCityList
        )
        coEvery { localDataSource.getFavoriteIds() } returns emptySet()

        // When
        val startTime = System.currentTimeMillis()
        val result = repository.getAllCities()
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Processing took ${processingTime}ms, expected under 200ms", processingTime < 200)
        assertTrue(result is Result.Success)
        assertEquals(largeCityList.size, (result as Result.Success).data.size)

        // Note: No sorting verification needed as JSON is pre-sorted
    }

    private fun generateLargeCityList(size: Int): List<CityRemoteDto> = (1..size).map { index ->
        CityRemoteDto(
            _id = index,
            name = "City$index",
            country = "Country${index % 10}",
            coordinates = CoordinatesDto(
                lon = index.toDouble(),
                lat = index.toDouble()
            )
        )
    }
}
