package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.SearchCitiesUseCase
import com.ualachallenge.ui.viewmodel.states.SearchState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CitySearchViewModelTest {

    private lateinit var viewModel: CitySearchViewModel
    private lateinit var searchCitiesUseCase: SearchCitiesUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val testCities = listOf(
        City(id = 1, name = "New York", country = "US", lat = 40.7128, lon = -74.0060, isFavorite = false),
        City(id = 2, name = "London", country = "UK", lat = 51.5074, lon = -0.1278, isFavorite = true),
        City(id = 3, name = "Paris", country = "France", lat = 48.8566, lon = 2.3522, isFavorite = false)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchCitiesUseCase = mockk()
        viewModel = CitySearchViewModel(searchCitiesUseCase = searchCitiesUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given searchCities is called with valid query, When use case returns success, Then should update state with results`() = runTest {
        // Given
        val query = "New"
        val searchResults = listOf(testCities[0]) // New York
        coEvery { searchCitiesUseCase(query, false) } returns Result.Success(searchResults)

        // When
        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Results }
        val resultsState = searchState as SearchState.Results
        assertEquals(searchResults, resultsState.cities)

        val searchQuery = viewModel.searchQuery.first()
        assertEquals(query, searchQuery)
    }

    @Test
    fun `Given searchCities is called with empty query, When query is empty, Then should set state to idle`() = runTest {
        // Given
        val query = ""

        // When
        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Idle }
        assertTrue(searchState is SearchState.Idle)

        val searchQuery = viewModel.searchQuery.first()
        assertEquals("", searchQuery)
    }

    @Test
    fun `Given searchCities is called with whitespace query, When query is only whitespace, Then should set state to idle`() = runTest {
        // Given
        val query = "   "

        // When
        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Idle }
        assertTrue(searchState is SearchState.Idle)

        val searchQuery = viewModel.searchQuery.first()
        assertEquals("", searchQuery)
    }

    @Test
    fun `Given searchCities is called with long query, When query exceeds max length, Then should set error state`() = runTest {
        // Given
        val query = "a".repeat(51) // Exceeds MAX_SEARCH_QUERY_LENGTH

        // When
        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Error }
        val errorState = searchState as SearchState.Error
        assertEquals("Search query too long", errorState.message)
    }

    @Test
    fun `Given searchCities is called, When use case returns error, Then should set error state`() = runTest {
        // Given
        val query = "test"
        val errorMessage = "Network error"
        coEvery { searchCitiesUseCase(query, false) } returns Result.Error(errorMessage)

        // When
        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Error }
        val errorState = searchState as SearchState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `Given searchCities is called with onlyFavorites true, When use case returns success, Then should search only favorites`() = runTest {
        // Given
        val query = "London"
        val searchResults = listOf(testCities[1]) // London (favorite)
        coEvery { searchCitiesUseCase(query, true) } returns Result.Success(searchResults)

        // When
        viewModel.searchCities(query, onlyFavorites = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Results }
        val resultsState = searchState as SearchState.Results
        assertEquals(searchResults, resultsState.cities)
    }

    @Test
    fun `Given clearSearch is called, When search is active, Then should clear search state`() = runTest {
        // Given
        val query = "test"
        coEvery { searchCitiesUseCase(query, false) } returns Result.Success(testCities)

        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify search is active
        val initialQuery = viewModel.searchQuery.first()
        assertEquals(query, initialQuery)

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Idle }
        assertTrue(searchState is SearchState.Idle)

        val searchQuery = viewModel.searchQuery.first()
        assertEquals("", searchQuery)
    }

    @Test
    fun `Given getCurrentQuery is called, When search query is set, Then should return current query`() = runTest {
        // Given
        val query = "test"
        coEvery { searchCitiesUseCase(query, false) } returns Result.Success(testCities)

        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val currentQuery = viewModel.getCurrentQuery()

        // Then
        assertEquals(query, currentQuery)
    }

    @Test
    fun `Given isSearching is called, When search is in progress, Then should return true`() = runTest {
        // Given
        val query = "test"
        coEvery { searchCitiesUseCase(query, false) } returns Result.Loading

        // When
        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSearching())
    }

    @Test
    fun `Given isSearching is called, When search is not in progress, Then should return false`() = runTest {
        // Given
        // Initial state

        // When & Then
        assertFalse(viewModel.isSearching())
    }

    @Test
    fun `Given getSearchResults is called, When search has results, Then should return results`() = runTest {
        // Given
        val query = "test"
        coEvery { searchCitiesUseCase(query, false) } returns Result.Success(testCities)

        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val results = viewModel.getSearchResults()

        // Then
        assertEquals(testCities, results)
    }

    @Test
    fun `Given getSearchResults is called, When search has no results, Then should return empty list`() = runTest {
        // Given
        // Initial state

        // When
        val results = viewModel.getSearchResults()

        // Then
        assertTrue(results.isEmpty())
    }

    @Test
    fun `Given getSearchError is called, When search has error, Then should return error message`() = runTest {
        // Given
        val query = "test"
        val errorMessage = "Network error"
        coEvery { searchCitiesUseCase(query, false) } returns Result.Error(errorMessage)

        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val error = viewModel.getSearchError()

        // Then
        assertEquals(errorMessage, error)
    }

    @Test
    fun `Given getSearchError is called, When search has no error, Then should return null`() = runTest {
        // Given
        // Initial state

        // When
        val error = viewModel.getSearchError()

        // Then
        assertNull(error)
    }

    @Test
    fun `Given refreshCurrentSearch is called, When previous search exists, Then should refresh search`() = runTest {
        // Given
        val query = "test"
        val initialResults = listOf(testCities[0])
        val refreshedResults = listOf(testCities[1])
        coEvery { searchCitiesUseCase(query, false) } returns Result.Success(initialResults) andThen Result.Success(refreshedResults)

        viewModel.searchCities(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refreshCurrentSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Results }
        val resultsState = searchState as SearchState.Results
        assertEquals(refreshedResults, resultsState.cities)
    }

    @Test
    fun `Given refreshCurrentSearch is called, When no previous search exists, Then should not perform search`() = runTest {
        // Given
        // No previous search

        // When
        viewModel.refreshCurrentSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first()
        assertTrue(searchState is SearchState.Idle)
    }

    @Test
    fun `Given searchCities is called multiple times rapidly, When debounce is active, Then should cancel previous search`() = runTest {
        // Given
        val query1 = "test1"
        val query2 = "test2"
        coEvery { searchCitiesUseCase(query1, false) } returns Result.Success(testCities)
        coEvery { searchCitiesUseCase(query2, false) } returns Result.Success(listOf(testCities[0]))

        // When
        viewModel.searchCities(query1)
        viewModel.searchCities(query2) // This should cancel the first search
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searchState = viewModel.searchState.first { it is SearchState.Results }
        val resultsState = searchState as SearchState.Results
        assertEquals(1, resultsState.cities.size) // Should have results from second search
    }
}
