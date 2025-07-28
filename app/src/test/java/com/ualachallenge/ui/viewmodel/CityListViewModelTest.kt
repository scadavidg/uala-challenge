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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListViewModelTest {

    private lateinit var viewModel: CityListViewModel
    private lateinit var loadAllCitiesUseCase: LoadAllCitiesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var getFavoriteCitiesUseCase: GetFavoriteCitiesUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testCities = listOf(
        City(id = 1, name = "New York", country = "US", lat = 40.7128, lon = -74.0060, isFavorite = false),
        City(id = 2, name = "London", country = "UK", lat = 51.5074, lon = -0.1278, isFavorite = true),
        City(id = 3, name = "Paris", country = "FR", lat = 48.8566, lon = 2.3522, isFavorite = false)
    )

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
    fun `loadCities should update state with cities`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(testCities, state.cities)
        assertEquals(testCities, state.filteredCities)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun `toggleFavorite should update city favorite status`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Success(Unit)

        // Load cities first
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        val newYork = state.cities.find { it.id == 1 }
        assertTrue(newYork?.isFavorite == true)
    }

    @Test
    fun `toggleShowOnlyFavorites should filter cities correctly`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)

        // Load cities first
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - toggle to show only favorites
        viewModel.toggleShowOnlyFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.showOnlyFavorites)
        assertEquals(1, state.filteredCities.size)
        assertEquals("London", state.filteredCities[0].name)
    }

    @Test
    fun `toggleShowOnlyFavorites should show all cities when toggled back`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)

        // Load cities first
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - toggle to show only favorites, then back to all
        viewModel.toggleShowOnlyFavorites()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleShowOnlyFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showOnlyFavorites)
        assertEquals(3, state.filteredCities.size)
    }

    @Test
    fun `loadCities should handle error correctly`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { loadAllCitiesUseCase() } returns Result.Error(errorMessage)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.cities.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `toggleFavorite should handle error correctly`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Error("Toggle failed")

        // Load cities first
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals("Toggle failed", state.error)
    }
}
