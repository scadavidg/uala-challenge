package com.domain.usecases

import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCityByIdUseCaseTest {

    private lateinit var useCase: GetCityByIdUseCase
    private lateinit var mockRepository: CityRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        useCase = GetCityByIdUseCase(mockRepository)
    }

    @Test
    fun `invoke should return city when found`() = runTest {
        // Given
        val city = City(1, "Bogotá", "Colombia", 4.7110, -74.0721, true)
        coEvery { mockRepository.getCityById(1) } returns Result.Success(city)

        // When
        val result = useCase(1)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(city, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return null when city not found`() = runTest {
        // Given
        coEvery { mockRepository.getCityById(999) } returns Result.Success(null)

        // When
        val result = useCase(999)

        // Then
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val errorMessage = "Failed to get city"
        coEvery { mockRepository.getCityById(1) } returns Result.Error(errorMessage)

        // When
        val result = useCase(1)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `invoke should call repository with correct cityId`() = runTest {
        // Given
        val cityId = 123
        val city = City(cityId, "Test City", "Test Country", 0.0, 0.0, false)
        coEvery { mockRepository.getCityById(cityId) } returns Result.Success(city)

        // When
        val result = useCase(cityId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(city, (result as Result.Success).data)
    }
} 