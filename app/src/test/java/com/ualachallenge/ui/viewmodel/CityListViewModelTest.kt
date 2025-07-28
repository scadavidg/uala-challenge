package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.LoadAllCitiesUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListViewModelTest {

    private lateinit var viewModel: CityListViewModel
    private lateinit var loadAllCitiesUseCase: LoadAllCitiesUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val mockCities = listOf(
        City(
            id = 1,
            name = "New York",
            country = "United States",
            lat = 40.7128,
            lon = -74.0060,
            isFavorite = true
        ),
        City(
            id = 2,
            name = "London",
            country = "United Kingdom",
            lat = 51.5074,
            lon = -0.1278,
            isFavorite = false
        ),
        City(
            id = 3,
            name = "Paris",
            country = "France",
            lat = 48.8566,
            lon = 2.3522,
            isFavorite = true
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loadAllCitiesUseCase = mockk()
        // Mock the initial call that happens in init
        coEvery { loadAllCitiesUseCase() } returns Result.Success(emptyList())
        viewModel = CityListViewModel(loadAllCitiesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(emptyList())

        // When
        val initialState = viewModel.uiState.first()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Then
        assertEquals(emptyList<City>(), state.cities)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadCities should update state to loading when called`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Loading

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Then
        assertTrue(state.isLoading)
    }

    @Test
    fun `loadCities should update state with cities on success`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(mockCities)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Then
        assertEquals(mockCities, state.cities)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadCities should update state with error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { loadAllCitiesUseCase() } returns Result.Error(errorMessage)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Then
        assertEquals(errorMessage, state.error)
        assertFalse(state.isLoading)
        assertEquals(emptyList<City>(), state.cities)
    }

    @Test
    fun `loadCities should clear previous error when called again`() = runTest {
        // Given - First call returns error
        coEvery { loadAllCitiesUseCase() } returns Result.Error("Network error")

        // When - First call
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.first()

        // Then - Verify error state
        assertEquals("Network error", state.error)

        // Given - Second call returns success
        coEvery { loadAllCitiesUseCase() } returns Result.Success(mockCities)

        // When - Second call
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.first()

        // Then - Verify error is cleared
        assertNull(state.error)
        assertEquals(mockCities, state.cities)
    }

    @Test
    fun `loadCities should be called automatically on init`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(mockCities)

        // When - Create new viewModel to trigger init
        val newViewModel = CityListViewModel(loadAllCitiesUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = newViewModel.uiState.first()

        // Then
        assertEquals(mockCities, state.cities)
    }
}
