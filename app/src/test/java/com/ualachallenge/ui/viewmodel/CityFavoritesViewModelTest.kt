package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityFavoritesViewModelTest {

    private lateinit var viewModel: CityFavoritesViewModel
    private lateinit var mockToggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var mockGetFavoriteCitiesUseCase: GetFavoriteCitiesUseCase
    private lateinit var mockDataViewModel: CityListDataViewModel
    private lateinit var mockSearchViewModel: CitySearchViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testCities = listOf(
        City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = true),
        City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false),
        City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = true)
    )

    @BeforeEach
    fun setup() {
        mockToggleFavoriteUseCase = mockk()
        mockGetFavoriteCitiesUseCase = mockk()
        mockDataViewModel = mockk(relaxed = true)
        mockSearchViewModel = mockk(relaxed = true)

        viewModel = CityFavoritesViewModel(mockToggleFavoriteUseCase, mockGetFavoriteCitiesUseCase)
        viewModel.setDataViewModel(mockDataViewModel)
        viewModel.setSearchViewModel(mockSearchViewModel)

        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFavoriteCities toggling state check`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Loading

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isToggling())
    }

    @Test
    fun `loadFavoriteCities success state check`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isToggling())
        assertEquals(testCities, viewModel.getFavoriteCities())
    }

    @Test
    fun `loadFavoriteCities error state check from use case`() = runTest {
        // Given
        val errorMessage = "Failed to load favorites"
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Error(errorMessage)

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.getError())
    }

    @Test
    fun `loadFavoriteCities loading state from use case check`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Loading

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isToggling())
    }

    @Test
    fun `loadFavoriteCities exception handling`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } throws RuntimeException("Network error")

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.getError()?.contains("Failed to load favorites") == true)
    }

    @Test
    fun `toggleFavorite success reloads favorites`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockToggleFavoriteUseCase(cityId) } returns Result.Success(Unit)
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModel.toggleFavorite(cityId)
        advanceUntilIdle()

        // Then
        verify { mockDataViewModel.updateCityFavoriteStatus(cityId, any()) }
        verify { mockSearchViewModel.refreshCurrentSearch() }
        assertEquals(testCities, viewModel.getFavoriteCities())
    }

    @Test
    fun `toggleFavorite error state from use case`() = runTest {
        // Given
        val cityId = 1
        val errorMessage = "Failed to toggle favorite"
        coEvery { mockToggleFavoriteUseCase(cityId) } returns Result.Error(errorMessage)

        // When
        viewModel.toggleFavorite(cityId)
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.getError())
    }

    @Test
    fun `toggleFavorite loading state from use case check`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockToggleFavoriteUseCase(cityId) } returns Result.Loading

        // When
        viewModel.toggleFavorite(cityId)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isToggling())
    }

    @Test
    fun `toggleFavorite exception handling`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockToggleFavoriteUseCase(cityId) } throws RuntimeException("Database error")

        // When
        viewModel.toggleFavorite(cityId)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.getError()?.contains("Failed to toggle favorite") == true)
    }

    @Test
    fun `getCurrentFavoriteStatus with non existent city`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Success(testCities)
        viewModel.loadFavoriteCities()
        advanceUntilIdle()

        // When
        val result = viewModel.getCurrentFavoriteStatus(999)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getFavoriteCities initial state`() {
        // Given - Initial state is empty

        // When
        val result = viewModel.getFavoriteCities()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFavoriteCities after loading`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        val result = viewModel.getFavoriteCities()

        // Then
        assertEquals(testCities, result)
    }

    @Test
    fun `isToggling when idle`() {
        // Given - Initial state is idle

        // When
        val result = viewModel.isToggling()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isToggling when toggling`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Loading

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        val result = viewModel.isToggling()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isToggling when error`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Error("Test error")

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        val result = viewModel.isToggling()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getError when idle`() {
        // Given - Initial state is idle

        // When
        val result = viewModel.getError()

        // Then
        assertNull(result)
    }

    @Test
    fun `getError when toggling`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Loading

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        val result = viewModel.getError()

        // Then
        assertNull(result)
    }

    @Test
    fun `getError when error`() = runTest {
        // Given
        val errorMessage = "Test error message"
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Error(errorMessage)

        // When
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        val result = viewModel.getError()

        // Then
        assertEquals(errorMessage, result)
    }

    @Test
    fun `clearError from error state`() = runTest {
        // Given
        val errorMessage = "Test error message"
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Error(errorMessage)
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        assertTrue(viewModel.getError() != null)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.getError())
        assertFalse(viewModel.isToggling())
    }

    @Test
    fun `clearError from idle state`() {
        // Given - Initial state is idle

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.getError())
        assertFalse(viewModel.isToggling())
    }

    @Test
    fun `clearError from toggling state`() = runTest {
        // Given
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Loading
        viewModel.loadFavoriteCities()
        advanceUntilIdle()
        assertTrue(viewModel.isToggling())

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.getError())
        assertFalse(viewModel.isToggling())
    }

    @Test
    fun `toggleFavorite without dataViewModel set`() = runTest {
        // Given
        val cityId = 1
        val viewModelWithoutData = CityFavoritesViewModel(mockToggleFavoriteUseCase, mockGetFavoriteCitiesUseCase)
        coEvery { mockToggleFavoriteUseCase(cityId) } returns Result.Success(Unit)
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModelWithoutData.toggleFavorite(cityId)
        advanceUntilIdle()

        // Then
        // Should not crash and should still work without dataViewModel
        assertEquals(testCities, viewModelWithoutData.getFavoriteCities())
    }

    @Test
    fun `toggleFavorite without searchViewModel set`() = runTest {
        // Given
        val cityId = 1
        val viewModelWithoutSearch = CityFavoritesViewModel(mockToggleFavoriteUseCase, mockGetFavoriteCitiesUseCase)
        viewModelWithoutSearch.setDataViewModel(mockDataViewModel)
        coEvery { mockToggleFavoriteUseCase(cityId) } returns Result.Success(Unit)
        coEvery { mockGetFavoriteCitiesUseCase() } returns Result.Success(testCities)

        // When
        viewModelWithoutSearch.toggleFavorite(cityId)
        advanceUntilIdle()

        // Then
        // Should not crash and should still work without searchViewModel
        verify { mockDataViewModel.updateCityFavoriteStatus(cityId, any()) }
        assertEquals(testCities, viewModelWithoutSearch.getFavoriteCities())
    }
}
