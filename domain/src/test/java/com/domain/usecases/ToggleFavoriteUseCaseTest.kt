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

class ToggleFavoriteUseCaseTest {

    private lateinit var useCase: ToggleFavoriteUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = ToggleFavoriteUseCase(mockRepository)
    }

    @Test
    fun `Given repository succeeds, When invoke is called, Then should return success`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockRepository.toggleFavorite(cityId) } returns Result.Success(Unit)

        // When
        val result = useCase(cityId)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `Given repository fails, When invoke is called, Then should return error`() = runTest {
        // Given
        val cityId = 1
        val errorMessage = "Toggle failed"
        coEvery { mockRepository.toggleFavorite(cityId) } returns Result.Error(errorMessage)

        // When
        val result = useCase(cityId)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `Given cityId is provided, When invoke is called, Then should call repository with correct cityId`() = runTest {
        // Given
        val cityId = 123
        coEvery { mockRepository.toggleFavorite(cityId) } returns Result.Success(Unit)

        // When
        val result = useCase(cityId)

        // Then
        assertTrue(result is Result.Success)
    }
}
