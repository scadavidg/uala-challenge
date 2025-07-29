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

class LoadAllCitiesUseCaseTest {

    private lateinit var useCase: LoadAllCitiesUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = LoadAllCitiesUseCase(mockRepository)
    }

    @Test
    fun `Given repository succeeds, When invoke is called, Then should return success with cities`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false)
        )
        coEvery { mockRepository.getAllCities(page = 1, limit = 20) } returns Result.Success(cities)

        // When
        val result = useCase(page = 1, limit = 20)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(cities, (result as Result.Success).data)
    }

    @Test
    fun `Given repository fails, When invoke is called, Then should return error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { mockRepository.getAllCities(page = 1, limit = 20) } returns Result.Error(errorMessage)

        // When
        val result = useCase(page = 1, limit = 20)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `Given repository returns empty list, When invoke is called, Then should return empty list`() = runTest {
        // Given
        coEvery { mockRepository.getAllCities(page = 1, limit = 20) } returns Result.Success(emptyList())

        // When
        val result = useCase(page = 1, limit = 20)

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }
}
