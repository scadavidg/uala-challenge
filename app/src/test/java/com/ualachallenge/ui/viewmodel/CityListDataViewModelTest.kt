package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.LoadAllCitiesUseCase
import com.ualachallenge.ui.viewmodel.states.CityListState
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Ignore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListDataViewModelTest {

    private lateinit var viewModel: CityListDataViewModel
    private lateinit var mockLoadAllCitiesUseCase: LoadAllCitiesUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val testCities = listOf(
        City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
        City(id = 2, name = "Alaska", country = "US", lat = 64.2008, lon = -149.4937, isFavorite = true),
        City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockLoadAllCitiesUseCase = mockk()
        viewModel = CityListDataViewModel(mockLoadAllCitiesUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Ignore
    @Test
    fun `Given loadCities is called, When use case returns loading, Then should update list state to loading`() = runTest {
        // Given
        coEvery { mockLoadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Loading

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.listState.value is CityListState.Loading)
        val listState = viewModel.listState.value
        assertTrue(listState is CityListState.Loading)
    }

    @Ignore
    @Test
    fun `Given loadCities is called, When use case returns success, Then should update list state to success`() = runTest {
        // Given
        coEvery { mockLoadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Success(testCities)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val listState = viewModel.listState.value
        assertTrue(listState is CityListState.Success)
        val successState = listState as CityListState.Success
        assertEquals(testCities, successState.cities)
    }

    @Ignore
    @Test
    fun `Given loadCities is called, When use case returns error, Then should update list state to error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { mockLoadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Error(errorMessage)

        // When
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val listState = viewModel.listState.value
        assertTrue(listState is CityListState.Error)
        val errorState = listState as CityListState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Ignore
    @Test
    fun `Given loadMoreCities is called, When use case returns loading, Then should update pagination state to loading`() = runTest {
        // Given
        coEvery { mockLoadAllCitiesUseCase(page = 2, limit = 20) } returns Result.Loading

        // When
        viewModel.loadMoreCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val paginationState = viewModel.paginationState.value
        assertTrue(paginationState.isLoadingMore)
    }

    @Ignore
    @Test
    fun `Given loadMoreCities is called, When use case returns success, Then should update pagination state to success`() = runTest {
        // Given
        val moreCities = listOf(
            City(id = 4, name = "Arizona", country = "US", lat = 33.7298, lon = -111.4312, isFavorite = false)
        )
        coEvery { mockLoadAllCitiesUseCase(page = 2, limit = 20) } returns Result.Success(moreCities)

        // When
        viewModel.loadMoreCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val paginationState = viewModel.paginationState.value
        assertTrue(!paginationState.isLoadingMore)
        assertTrue(paginationState.hasMoreData)
    }

    @Ignore
    @Test
    fun `Given loadMoreCities is called, When use case returns empty list, Then should update pagination state to no more data`() = runTest {
        // Given
        coEvery { mockLoadAllCitiesUseCase(page = 2, limit = 20) } returns Result.Success(emptyList())

        // When
        viewModel.loadMoreCities()

        // Then
        val paginationState = viewModel.paginationState.value
        assertTrue(!paginationState.isLoadingMore)
        assertTrue(!paginationState.hasMoreData)
    }

    @Ignore
    @Test
    fun `Given loadMoreCities is called, When use case returns error, Then should update pagination state to error`() = runTest {
        // Given
        val errorMessage = "Failed to load more cities"
        coEvery { mockLoadAllCitiesUseCase(page = 2, limit = 20) } returns Result.Error(errorMessage)

        // When
        viewModel.loadMoreCities()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val paginationState = viewModel.paginationState.value
        assertTrue(!paginationState.isLoadingMore)
        assertTrue(!paginationState.hasMoreData)
    }

    @Ignore
    @Test
    fun `Given updateCityFavoriteStatus is called, When city exists in list, Then should update city favorite status`() = runTest {
        // Given
        coEvery { mockLoadAllCitiesUseCase(page = 1, limit = 20) } returns Result.Success(testCities)
        viewModel.loadCities()
        testDispatcher.scheduler.advanceUntilIdle()

        val cityId = 1
        val newFavoriteStatus = true

        // When
        viewModel.updateCityFavoriteStatus(cityId, newFavoriteStatus)

        // Then
        val listState = viewModel.listState.value
        assertTrue(listState is CityListState.Success)
        val successState = listState as CityListState.Success
        val updatedCity = successState.cities.find { it.id == cityId }
        assertEquals(newFavoriteStatus, updatedCity?.isFavorite)
    }

    @Ignore
    @Test
    fun `Given loadAllCitiesForFavorites is called, When use case returns success, Then should load all cities without pagination`() = runTest {
        // Given
        coEvery { mockLoadAllCitiesUseCase(1, Int.MAX_VALUE) } returns Result.Success(testCities)

        // When
        viewModel.loadAllCitiesForFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val listState = viewModel.listState.value
        assertTrue(listState is CityListState.Success)
        val successState = listState as CityListState.Success
        assertEquals(testCities, successState.cities)
    }

    @Test
    fun `Given viewModel is created, When initialized, Then should have correct initial state`() {
        // Given - When (ViewModel is created in setup)

        // Then
        val listState = viewModel.listState.value
        val paginationState = viewModel.paginationState.value

        assertTrue(listState is CityListState.Loading)
        assertTrue(!paginationState.isLoadingMore)
        assertTrue(!paginationState.hasMoreData)
    }
}
