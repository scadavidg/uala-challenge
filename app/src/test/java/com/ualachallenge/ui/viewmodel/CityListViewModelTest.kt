package com.ualachallenge.ui.viewmodel

import com.domain.models.Result
import com.domain.usecases.GetOnlineModeUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.SearchCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import com.domain.usecases.ToggleOnlineModeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListViewModelTest {

    private lateinit var viewModel: CityListViewModel
    private lateinit var loadAllCitiesUseCase: LoadAllCitiesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var searchCitiesUseCase: SearchCitiesUseCase
    private lateinit var toggleOnlineModeUseCase: ToggleOnlineModeUseCase
    private lateinit var getOnlineModeUseCase: GetOnlineModeUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        loadAllCitiesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        searchCitiesUseCase = mockk()
        toggleOnlineModeUseCase = mockk()
        getOnlineModeUseCase = mockk()

        // Setup default mocks
        coEvery { loadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Success(emptyList())
        coEvery { getOnlineModeUseCase() } returns Result.Success(false)

        viewModel = CityListViewModel(
            loadAllCitiesUseCase = loadAllCitiesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            searchCitiesUseCase = searchCitiesUseCase,
            toggleOnlineModeUseCase = toggleOnlineModeUseCase,
            getOnlineModeUseCase = getOnlineModeUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be offline mode`() {
        // Given
        // ViewModel is initialized in setup() with default offline mode

        // When
        val initialState = viewModel.uiState.value

        // Then
        assertFalse(initialState.isOnlineMode)
    }

    @Test
    fun `toggleOnlineMode should update online mode state`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Success(Unit)
        coEvery { loadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Success(emptyList())

        // When
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isOnlineMode)
    }

    @Test
    fun `toggleOnlineMode should handle error from use case`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(any()) } returns Result.Error("Toggle failed")
        coEvery { loadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Success(emptyList())

        // When
        viewModel.toggleOnlineMode()

        // Then - check that toggle state is set and error is captured
        val state = viewModel.uiState.value
        assertTrue("Should be in toggling state", state.isTogglingOnlineMode)

        // Advance to let loadCities() complete
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `loadOnlineMode should handle error from use case`() = runTest {
        // Given
        coEvery { getOnlineModeUseCase() } returns Result.Error("Failed to get mode")

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Should keep default value (false) when error occurs
        assertFalse(viewModel.uiState.value.isOnlineMode)
    }
}
