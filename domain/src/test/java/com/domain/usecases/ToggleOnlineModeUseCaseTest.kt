package com.domain.usecases

import com.domain.models.Result
import com.domain.repositories.CityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ToggleOnlineModeUseCaseTest {

    private lateinit var useCase: ToggleOnlineModeUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = ToggleOnlineModeUseCase(mockRepository)
    }

    @Test
    fun `Given repository succeeds, When invoke is called, Then should return success`() = runTest {
        // Given
        coEvery { mockRepository.toggleOnlineMode(true) } returns Result.Success(Unit)

        // When
        val result = useCase(true)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `Given repository fails, When invoke is called, Then should return error`() = runTest {
        // Given
        val errorMessage = "Toggle failed"
        coEvery { mockRepository.toggleOnlineMode(false) } returns Result.Error(errorMessage)

        // When
        val result = useCase(false)

        // Then
        assertTrue(result is Result.Error)
    }
}
