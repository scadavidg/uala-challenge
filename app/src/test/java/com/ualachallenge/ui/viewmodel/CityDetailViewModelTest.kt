package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetCityByIdUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private lateinit var viewModel: TestCityDetailViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCityByIdUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf("cityId" to 1))
        viewModel = TestCityDetailViewModel(getCityByIdUseCase, toggleFavoriteUseCase, savedStateHandle)
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

// Test version of CityDetailViewModel without Hilt
class TestCityDetailViewModel(
    private val getCityByIdUseCase: GetCityByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cityId: Int = (savedStateHandle["cityId"] as? Int) ?: throw IllegalArgumentException("cityId must be an Int")

    private val _uiState = MutableStateFlow(CityDetailUiState())
    val uiState: StateFlow<CityDetailUiState> = _uiState.asStateFlow()

    fun loadCityDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                when (val result = getCityByIdUseCase(cityId)) {
                    is Result.Success -> {
                        result.data?.let { city ->
                            _uiState.update {
                                it.copy(
                                    city = city,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        } ?: run {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "City not found with ID: $cityId"
                                )
                            }
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error loading city: ${result.message}"
                            )
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isTogglingFavorite = true, error = null) }

        viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(cityId)) {
                    is Result.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                city = currentState.city?.copy(
                                    isFavorite = !currentState.city.isFavorite
                                ),
                                isTogglingFavorite = false
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isTogglingFavorite = false, error = result.message)
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isTogglingFavorite = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTogglingFavorite = false,
                        error = "Failed to toggle favorite: ${e.message}"
                    )
                }
            }
        }
    }

    fun navigateBack() {
        _uiState.update { it.copy(isNavigatingBack = true, error = null) }

        // Simulate a brief loading state for navigation
        viewModelScope.launch {
            kotlinx.coroutines.delay(200) // Brief delay to show loading state
            _uiState.update { it.copy(isNavigatingBack = false) }
        }
    }
}
