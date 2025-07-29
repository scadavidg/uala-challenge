package com.domain.usecases

import com.domain.models.Result
import com.domain.repositories.CityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetOnlineModeUseCaseTest {

    private lateinit var useCase: GetOnlineModeUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = GetOnlineModeUseCase(mockRepository)
    }

    @Test
    fun `invoke should return success when repository succeeds`() = runTest {
        // Given
        coEvery { mockRepository.isOnlineMode() } returns Result.Success(true)

        // When
        val result = useCase()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val errorMessage = "Failed to get online mode"
        coEvery { mockRepository.isOnlineMode() } returns Result.Error(errorMessage)

        // When
        val result = useCase()

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }
}
