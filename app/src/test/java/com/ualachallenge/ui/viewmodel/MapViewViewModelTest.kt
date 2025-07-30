package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetCityByIdUseCase
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewViewModelTest {

    private lateinit var getCityByIdUseCase: GetCityByIdUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var favoriteNotificationViewModel: FavoriteNotificationViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: MapViewViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCityByIdUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        favoriteNotificationViewModel = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("cityId" to 1))
        viewModel = MapViewViewModel(getCityByIdUseCase, toggleFavoriteUseCase, favoriteNotificationViewModel, savedStateHandle)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given city is loaded successfully, When loadCityDetails is called, Then uiState should contain city data`() = runTest {
        // Given
        val testCity = City(
            id = 1,
            name = "Test City",
            country = "Test Country",
            lat = 40.0,
            lon = -74.0,
            isFavorite = false
        )
        coEvery { getCityByIdUseCase(1) } returns Result.Success(testCity)

        // When
        viewModel.loadCityDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertNotNull(uiState.city)
        assertEquals(testCity, uiState.city)
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }

    @Test
    fun `Given city is not found, When loadCityDetails is called, Then uiState should contain error`() = runTest {
        // Given
        coEvery { getCityByIdUseCase(1) } returns Result.Success(null)

        // When
        viewModel.loadCityDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.city)
        assertFalse(uiState.isLoading)
        assertEquals("City not found with ID: 1", uiState.error)
    }

    @Test
    fun `Given use case returns error, When loadCityDetails is called, Then uiState should contain error`() = runTest {
        // Given
        coEvery { getCityByIdUseCase(1) } returns Result.Error("Network error")

        // When
        viewModel.loadCityDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.city)
        assertFalse(uiState.isLoading)
        assertEquals("Error loading city: Network error", uiState.error)
    }

    @Test
    fun `Given exception occurs, When loadCityDetails is called, Then uiState should contain error`() = runTest {
        // Given
        coEvery { getCityByIdUseCase(1) } throws RuntimeException("Unexpected error")

        // When
        viewModel.loadCityDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.city)
        assertFalse(uiState.isLoading)
        assertEquals("Unexpected error: Unexpected error", uiState.error)
    }

    @Test
    fun `Given toggle favorite is successful, When toggleFavorite is called, Then city favorite status should be updated`() = runTest {
        // Given
        val testCity = City(
            id = 1,
            name = "Test City",
            country = "Test Country",
            lat = 40.0,
            lon = -74.0,
            isFavorite = false
        )
        coEvery { getCityByIdUseCase(1) } returns Result.Success(testCity)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Success(Unit)

        // Load city first
        viewModel.loadCityDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertNotNull(uiState.city)
        assertTrue(uiState.city!!.isFavorite)
    }

    @Test
    fun `Given toggle favorite fails, When toggleFavorite is called, Then error should be set`() = runTest {
        // Given
        val testCity = City(
            id = 1,
            name = "Test City",
            country = "Test Country",
            lat = 40.0,
            lon = -74.0,
            isFavorite = false
        )
        coEvery { getCityByIdUseCase(1) } returns Result.Success(testCity)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Error("Toggle failed")

        // Load city first
        viewModel.loadCityDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Toggle failed", uiState.error)
    }
}
