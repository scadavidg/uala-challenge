package com.ualachallenge.ui.viewmodel

import com.data.local.migration.JsonToRoomMigrationService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CacheMigrationViewModelTest {
    private lateinit var viewModel: CacheMigrationViewModel
    private lateinit var mockMigrationService: JsonToRoomMigrationService

    @BeforeEach
    fun setup() {
        mockMigrationService = mockk()
        coEvery { mockMigrationService.migrationProgress } returns MutableStateFlow(0f)
        coEvery { mockMigrationService.isMigrationInProgress } returns MutableStateFlow(false)
        viewModel = CacheMigrationViewModel(mockMigrationService)
    }

    @Test
    fun `Given migration is not running, When isMigrationRunning is called, Then should return false`() {
        // Given
        coEvery { mockMigrationService.isMigrationInProgress } returns MutableStateFlow(false)

        // When
        val result = viewModel.isMigrationRunning()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given migration completed state is true, When resetMigrationCompleted is called, Then should reset to false`() {
        // Given
        // Simulate migration completed state
        viewModel.resetMigrationCompleted()

        // When
        val result = viewModel.migrationCompleted.value

        // Then
        assertFalse(result)
    }
}
