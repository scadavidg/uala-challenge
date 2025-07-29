package com.data.repositories

import com.data.local.migration.JsonToRoomMigrationService
import com.domain.models.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DatabaseInitializationRepositoryImplTest {
    private lateinit var repository: DatabaseInitializationRepositoryImpl
    private lateinit var mockMigrationService: JsonToRoomMigrationService

    @BeforeEach
    fun setup() {
        mockMigrationService = mockk()
        repository = DatabaseInitializationRepositoryImpl(mockMigrationService)
    }

    @Test
    fun `Given migration service returns success, When initializeDatabase is called, Then should return success result`() = runTest {
        // Given
        coEvery { mockMigrationService.migrateIfNeeded() } returns true

        // When
        val result = repository.initializeDatabase()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
    }

    @Test
    fun `Given migration service returns failure, When initializeDatabase is called, Then should return error result`() = runTest {
        // Given
        coEvery { mockMigrationService.migrateIfNeeded() } returns false

        // When
        val result = repository.initializeDatabase()

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Failed to initialize Room database", (result as Result.Error).message)
    }

    @Test
    fun `Given migrationService throws exception,When initializeDatabase is called,Then should return errorResult with exceptionMessage`() = runTest {
        // Given
        val exceptionMessage = "Migration failed"
        coEvery { mockMigrationService.migrateIfNeeded() } throws Exception(exceptionMessage)

        // When
        val result = repository.initializeDatabase()

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Error initializing Room database: $exceptionMessage", (result as Result.Error).message)
    }
}
