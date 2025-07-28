package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class CityListViewModelPerformanceTest {
    private lateinit var viewModel: CityListViewModel
    private lateinit var loadAllCitiesUseCase: LoadAllCitiesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var getFavoriteCitiesUseCase: GetFavoriteCitiesUseCase
    private val testDispatcher = StandardTestDispatcher()

    private fun generateLargeCityList(size: Int): List<City> {
        return (1..size).map { index ->
            City(
                id = index,
                name = "City $index",
                country = "Country $index",
                lat = 40.0 + (index * 0.1),
                lon = -74.0 + (index * 0.1),
                isFavorite = index % 2 == 0
            )
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loadAllCitiesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        getFavoriteCitiesUseCase = mockk()

        // Mock the initial call that happens in init
        coEvery { loadAllCitiesUseCase() } returns Result.Success(emptyList())

        viewModel = CityListViewModel(
            loadAllCitiesUseCase,
            toggleFavoriteUseCase,
            getFavoriteCitiesUseCase
        )

        // Wait for initial load to complete
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCities_handlesLargeDataset_under500ms() = runTest {
        // Given
        val largeCityList = generateLargeCityList(1000)
        coEvery { loadAllCitiesUseCase() } returns Result.Success(largeCityList)

        // When
        val startTime = System.currentTimeMillis()
        viewModel.loadCities()
        val uiState = viewModel.uiState.first { it.cities.isNotEmpty() }
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Large dataset processing took ${processingTime}ms, expected under 500ms", processingTime < 500)
        assertEquals(largeCityList.size, uiState.cities.size)
    }

    @Test
    fun loadCities_handlesMediumDataset_under200ms() = runTest {
        // Given
        val mediumCityList = generateLargeCityList(500)
        coEvery { loadAllCitiesUseCase() } returns Result.Success(mediumCityList)

        // When
        val startTime = System.currentTimeMillis()
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val uiState = viewModel.uiState.first { it.cities.isNotEmpty() }
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Medium dataset processing took ${processingTime}ms, expected under 200ms", processingTime < 200)
        assertEquals(mediumCityList.size, uiState.cities.size)
    }

    @Test
    fun loadCities_memoryEfficient_largeDataset() = runTest {
        // Given
        val largeCityList = generateLargeCityList(2000)
        coEvery { loadAllCitiesUseCase() } returns Result.Success(largeCityList)
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val uiState = viewModel.uiState.first { it.cities.isNotEmpty() }
        System.gc()
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory
        val memoryUsedMB = memoryUsed / (1024 * 1024)

        // Then
        assertTrue("Memory usage: ${memoryUsedMB}MB, expected under 10MB", memoryUsedMB < 10)
        assertEquals(largeCityList.size, uiState.cities.size)
    }

    @Test
    fun loadCities_stateUpdates_under100ms() = runTest {
        // Given
        val cityList = generateLargeCityList(100)
        coEvery { loadAllCitiesUseCase() } returns Result.Success(cityList)

        // When
        val startTime = System.currentTimeMillis()
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        val loadingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Loading state took ${loadingTime}ms, expected under 100ms", loadingTime < 100)
        assertEquals(cityList.size, state.cities.size)
    }

    @Test
    fun loadCities_errorHandling_under50ms() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Error("Test error")

        // When
        val startTime = System.currentTimeMillis()
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val errorState = viewModel.uiState.first { it.error != null }
        val errorTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Error handling took ${errorTime}ms, expected under 50ms", errorTime < 50)
        assertEquals("Test error", errorState.error)
    }

    @Test
    fun toggleFavorite_performance_under50ms() = runTest {
        // Given
        val cityList = generateLargeCityList(100)
        coEvery { loadAllCitiesUseCase() } returns Result.Success(cityList)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Success(Unit)

        // Load cities first
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val startTime = System.currentTimeMillis()
        viewModel.toggleFavorite(1)
        testDispatcher.scheduler.advanceUntilIdle()
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Toggle favorite took ${processingTime}ms, expected under 50ms", processingTime < 50)
    }

    @Test
    fun toggleShowOnlyFavorites_performance_under20ms() = runTest {
        // Given
        val cityList = generateLargeCityList(100)
        coEvery { loadAllCitiesUseCase() } returns Result.Success(cityList)

        // Load cities first
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val startTime = System.currentTimeMillis()
        viewModel.toggleShowOnlyFavorites()
        testDispatcher.scheduler.advanceUntilIdle()
        val processingTime = System.currentTimeMillis() - startTime

        // Then
        assertTrue("Toggle show only favorites took ${processingTime}ms, expected under 20ms", processingTime < 20)
    }
}
