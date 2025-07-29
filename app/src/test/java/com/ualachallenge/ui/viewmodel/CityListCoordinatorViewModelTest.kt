package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.ualachallenge.ui.viewmodel.states.CityFilters
import com.ualachallenge.ui.viewmodel.states.CityListState
import com.ualachallenge.ui.viewmodel.states.FavoritesState
import com.ualachallenge.ui.viewmodel.states.OnlineModeState
import com.ualachallenge.ui.viewmodel.states.PaginationState
import com.ualachallenge.ui.viewmodel.states.SearchState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityListCoordinatorViewModelTest {

    private lateinit var coordinator: CityListCoordinatorViewModel

    private val testCities = listOf(
        City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
        City(id = 2, name = "Alaska", country = "US", lat = 64.2008, lon = -149.4937, isFavorite = true),
        City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false)
    )

    private val testFavoriteCities = listOf(
        City(id = 2, name = "Alaska", country = "US", lat = 64.2008, lon = -149.4937, isFavorite = true)
    )

    @BeforeEach
    fun setup() {
        coordinator = CityListCoordinatorViewModel()
    }

    @Test
    fun `Given all states are idle, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Loading
        val paginationState = PaginationState()
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters()
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertTrue(uiState.isLoading)
        assertTrue(uiState.cities.isEmpty())
        assertTrue(uiState.filteredCities.isEmpty())
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given list state is success, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState()
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters()
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(testCities.size, uiState.filteredCities.size)
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given list state is error, When combineStates is called, Then should return correct UI state`() {
        // Given
        val errorMessage = "Network error"
        val listState = CityListState.Error(errorMessage)
        val paginationState = PaginationState()
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters()
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertTrue(uiState.cities.isEmpty())
        assertTrue(uiState.filteredCities.isEmpty())
        assertEquals(errorMessage, uiState.error)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given search state is searching, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState()
        val searchState = SearchState.Searching
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters(searchQuery = "test")
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(0, uiState.filteredCities.size) // When searching, filtered cities should be empty until results arrive
        assertTrue(uiState.error == null)
        assertEquals("test", uiState.searchQuery)
        assertTrue(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given search state has results, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState()
        val searchResults = listOf(testCities[0]) // Only Alabama
        val searchState = SearchState.Results(searchResults)
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters(searchQuery = "Alabama")
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(1, uiState.filteredCities.size)
        assertEquals("Alabama", uiState.filteredCities[0].name)
        assertTrue(uiState.error == null)
        assertEquals("Alabama", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given favorites state is toggling, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState()
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Toggling
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters()
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(testCities.size, uiState.filteredCities.size)
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertTrue(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given online mode state is toggling, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState()
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Toggling
        val filters = CityFilters()
        val isOnlineMode = true
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(testCities.size, uiState.filteredCities.size)
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertTrue(uiState.isTogglingOnlineMode)
        assertTrue(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given show only favorites is true, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState()
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters(showOnlyFavorites = true)
        val isOnlineMode = false
        val favoriteCities = testFavoriteCities

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(1, uiState.filteredCities.size)
        assertEquals("Alaska", uiState.filteredCities[0].name)
        assertTrue(uiState.filteredCities[0].isFavorite)
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertTrue(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given pagination state has more data, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState(
            currentPage = 2,
            hasMoreData = true,
            isLoadingMore = false
        )
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters()
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(testCities.size, uiState.filteredCities.size)
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertFalse(uiState.isLoadingMore)
        assertTrue(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given pagination state is loading more, When combineStates is called, Then should return correct UI state`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState(
            currentPage = 2,
            hasMoreData = true,
            isLoadingMore = true
        )
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters()
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoading)
        assertEquals(testCities.size, uiState.cities.size)
        assertEquals(testCities.size, uiState.filteredCities.size)
        assertTrue(uiState.error == null)
        assertEquals("", uiState.searchQuery)
        assertFalse(uiState.isSearching)
        assertFalse(uiState.showOnlyFavorites)
        assertFalse(uiState.isTogglingFavorites)
        assertFalse(uiState.isTogglingOnlineMode)
        assertFalse(uiState.isOnlineMode)
        assertTrue(uiState.isLoadingMore)
        assertTrue(uiState.hasMoreData)
        assertFalse(uiState.isNavigatingToDetail)
    }

    @Test
    fun `Given show only favorites is true, When combineStates is called, Then should disable load more`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState(
            currentPage = 2,
            hasMoreData = true,
            isLoadingMore = true
        )
        val searchState = SearchState.Idle
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters(showOnlyFavorites = true)
        val isOnlineMode = false
        val favoriteCities = testFavoriteCities

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
    }

    @Test
    fun `Given search results are not empty, When combineStates is called, Then should disable load more`() {
        // Given
        val listState = CityListState.Success(testCities)
        val paginationState = PaginationState(
            currentPage = 2,
            hasMoreData = true,
            isLoadingMore = true
        )
        val searchResults = listOf(testCities[0])
        val searchState = SearchState.Results(searchResults)
        val favoritesState = FavoritesState.Idle
        val onlineModeState = OnlineModeState.Idle
        val filters = CityFilters(searchQuery = "Alabama")
        val isOnlineMode = false
        val favoriteCities = emptyList<City>()

        // When
        val uiState = coordinator.combineStates(
            listState = listState,
            paginationState = paginationState,
            searchState = searchState,
            favoritesState = favoritesState,
            onlineModeState = onlineModeState,
            filters = filters,
            isOnlineMode = isOnlineMode,
            favoriteCities = favoriteCities
        )

        // Then
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.hasMoreData)
    }
}
