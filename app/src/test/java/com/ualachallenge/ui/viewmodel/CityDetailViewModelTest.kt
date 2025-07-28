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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityDetailViewModelTest {

    private lateinit var getCityByIdUseCase: GetCityByIdUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: CityDetailViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCityByIdUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf("cityId" to 1))
        viewModel = CityDetailViewModel(getCityByIdUseCase, toggleFavoriteUseCase, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when city is loaded successfully, uiState should contain city data`() = runTest {
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
    fun `when city is not found, uiState should contain error`() = runTest {
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
    fun `when use case returns error, uiState should contain error`() = runTest {
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
    fun `when toggle favorite is successful, city favorite status should be updated`() = runTest {
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
    fun `when toggle favorite fails, error should be set`() = runTest {
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
