package com.ualachallenge.ui.viewmodel

import com.domain.models.Result
import com.domain.usecases.GetOnlineModeUseCase
import com.domain.usecases.ToggleOnlineModeUseCase
import com.ualachallenge.ui.viewmodel.states.OnlineModeState
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityOnlineModeViewModelTest {

    private lateinit var viewModel: CityOnlineModeViewModel
    private lateinit var toggleOnlineModeUseCase: ToggleOnlineModeUseCase
    private lateinit var getOnlineModeUseCase: GetOnlineModeUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        toggleOnlineModeUseCase = mockk()
        getOnlineModeUseCase = mockk()

        // Mock the initial call that happens in init
        coEvery { getOnlineModeUseCase() } returns Result.Success(false)

        viewModel = CityOnlineModeViewModel(
            toggleOnlineModeUseCase = toggleOnlineModeUseCase,
            getOnlineModeUseCase = getOnlineModeUseCase
        )

        // Wait for initial load to complete
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given viewModel is initialized, When created, Then should load online mode`() = runTest {
        // Given
        // ViewModel is initialized in setup()

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertFalse(isOnlineMode)
    }

    @Test
    fun `Given loadOnlineMode is called, When use case returns success, Then should update online mode`() = runTest {
        // Given
        coEvery { getOnlineModeUseCase() } returns Result.Success(true)

        // When
        viewModel.loadOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertTrue(isOnlineMode)
    }

    @Test
    fun `Given toggleOnlineMode is called, When use case returns success, Then should toggle online mode`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Success(Unit)

        // When
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertTrue(isOnlineMode)

        val onlineModeState = viewModel.onlineModeState.first()
        assertTrue(onlineModeState is OnlineModeState.Idle)
    }

    @Test
    fun `Given toggleOnlineMode is called when online, When use case returns success, Then should turn off online mode`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Success(Unit)
        coEvery { toggleOnlineModeUseCase(false) } returns Result.Success(Unit)

        // Turn on online mode first
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertFalse(isOnlineMode)

        val onlineModeState = viewModel.onlineModeState.first()
        assertTrue(onlineModeState is OnlineModeState.Idle)
    }

    @Test
    fun `Given toggleOnlineMode is called, When use case returns error, Then should update state with error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Error(errorMessage)

        // When
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val onlineModeState = viewModel.onlineModeState.first { it is OnlineModeState.Error }
        val errorState = onlineModeState as OnlineModeState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `Given toggleOnlineMode is called, When use case returns loading, Then should set toggling state`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Loading

        // When
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val onlineModeState = viewModel.onlineModeState.first { it is OnlineModeState.Toggling }
        assertTrue(onlineModeState is OnlineModeState.Toggling)
    }

    @Test
    fun `Given isToggling is called, When state is toggling, Then should return true`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Loading

        // When
        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isToggling())
    }

    @Test
    fun `Given isToggling is called, When state is idle, Then should return false`() = runTest {
        // Given
        // Initial state is idle

        // When & Then
        assertFalse(viewModel.isToggling())
    }

    @Test
    fun `Given getError is called, When state has error, Then should return error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Error(errorMessage)

        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val error = viewModel.getError()

        // Then
        assertEquals(errorMessage, error)
    }

    @Test
    fun `Given getError is called, When state has no error, Then should return null`() = runTest {
        // Given
        // Initial state has no error

        // When
        val error = viewModel.getError()

        // Then
        assertNull(error)
    }

    @Test
    fun `Given clearError is called, When state has error, Then should clear error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Error(errorMessage)

        viewModel.toggleOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error exists
        assertEquals(errorMessage, viewModel.getError())

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.getError())

        val onlineModeState = viewModel.onlineModeState.first()
        assertTrue(onlineModeState is OnlineModeState.Idle)
    }

    @Test
    fun `Given toggleOnlineMode is called multiple times rapidly, When use case is slow, Then should handle correctly`() = runTest {
        // Given
        coEvery { toggleOnlineModeUseCase(true) } returns Result.Loading andThen Result.Success(Unit)

        // When
        viewModel.toggleOnlineMode()
        viewModel.toggleOnlineMode() // Second call while first is still loading
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertTrue(isOnlineMode)

        val onlineModeState = viewModel.onlineModeState.first()
        assertTrue(onlineModeState is OnlineModeState.Idle)
    }

    @Test
    fun `Given loadOnlineMode is called, When use case returns error, Then should keep default value`() = runTest {
        // Given
        coEvery { getOnlineModeUseCase() } returns Result.Error("Network error")

        // When
        viewModel.loadOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertFalse(isOnlineMode) // Should keep default value
    }

    @Test
    fun `Given loadOnlineMode is called, When use case returns loading, Then should keep default value`() = runTest {
        // Given
        coEvery { getOnlineModeUseCase() } returns Result.Loading

        // When
        viewModel.loadOnlineMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val isOnlineMode = viewModel.isOnlineMode.first()
        assertFalse(isOnlineMode) // Should keep default value
    }
}
