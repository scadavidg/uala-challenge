package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.SearchCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListViewModelTest {

    private lateinit var viewModel: CityListViewModel
    private lateinit var loadAllCitiesUseCase: LoadAllCitiesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var getFavoriteCitiesUseCase: GetFavoriteCitiesUseCase
    private lateinit var searchCitiesUseCase: SearchCitiesUseCase

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
        searchCitiesUseCase = mockk()

        coEvery { loadAllCitiesUseCase() } returns Result.Success(emptyList())

        viewModel = CityListViewModel(
            loadAllCitiesUseCase,
            toggleFavoriteUseCase,
            getFavoriteCitiesUseCase,
            searchCitiesUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchCities should update search query`() = runTest {
        // Given
        val searchQuery = "New"
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)
        coEvery { searchCitiesUseCase(searchQuery, false) } returns Result.Success(listOf(testCities[0]))

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.searchCities(searchQuery)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(searchQuery, state.searchQuery)
    }

    @Test
    fun `searchCities with empty query should show all cities`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.searchCities("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
        assertEquals(testCities, state.filteredCities)
    }

    @Test
    fun `toggleShowOnlyFavorites should filter cities`() = runTest {
        // Given
        coEvery { loadAllCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleShowOnlyFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.showOnlyFavorites)
        assertEquals(1, state.filteredCities.size) // Only London is favorite
        assertTrue(state.filteredCities.all { it.isFavorite })
    }
}
