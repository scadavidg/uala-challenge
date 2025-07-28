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

class SearchCitiesUseCaseTest {

    private lateinit var useCase: SearchCitiesUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = SearchCitiesUseCase(mockRepository)
    }

    @Test
    fun `invoke should return filtered cities when repository succeeds`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true),
            City(2, "Medellín", "Colombia", 6.2442, -75.5812, false)
        )
        coEvery { mockRepository.searchCities("bo", false) } returns Result.Success(cities)

        // When
        val result = useCase("bo", false)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(cities, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val errorMessage = "Search failed"
        coEvery { mockRepository.searchCities("bo", false) } returns Result.Error(errorMessage)

        // When
        val result = useCase("bo", false)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `invoke should search with onlyFavorites parameter`() = runTest {
        // Given
        val cities = listOf(
            City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true)
        )
        coEvery { mockRepository.searchCities("bo", true) } returns Result.Success(cities)

        // When
        val result = useCase("bo", true)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(cities, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return empty list when no cities match`() = runTest {
        // Given
        coEvery { mockRepository.searchCities("xyz", false) } returns Result.Success(emptyList())

        // When
        val result = useCase("xyz", false)

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }
}
