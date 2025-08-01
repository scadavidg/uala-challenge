package com.domain.usecases

import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetFavoriteCitiesUseCaseTest {

    private lateinit var useCase: GetFavoriteCitiesUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = GetFavoriteCitiesUseCase(mockRepository)
    }

    @Test
    fun `Given repository succeeds, When invoke is called, Then should return favorite cities`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, true)
        )
        coEvery { mockRepository.getFavoriteCities() } returns Result.Success(cities)

        // When
        val result = useCase()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(cities, (result as Result.Success).data)
        assertTrue(cities.all { it.isFavorite })
    }

    @Test
    fun `Given repository fails, When invoke is called, Then should return error`() = runTest {
        // Given
        val errorMessage = "Failed to get favorites"
        coEvery { mockRepository.getFavoriteCities() } returns Result.Error(errorMessage)

        // When
        val result = useCase()

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `Given no favorite cities exist, When invoke is called, Then should return empty list`() = runTest {
        // Given
        coEvery { mockRepository.getFavoriteCities() } returns Result.Success(emptyList())

        // When
        val result = useCase()

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }
}
