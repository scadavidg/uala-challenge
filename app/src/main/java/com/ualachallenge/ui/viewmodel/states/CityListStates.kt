package com.ualachallenge.ui.viewmodel.states

import com.domain.models.City

// States for the city list
sealed class CityListState {
    object Loading : CityListState()
    data class Success(val cities: List<City>) : CityListState()
    data class Error(val message: String) : CityListState()
}

// States for search
sealed class SearchState {
    object Idle : SearchState()
    object Searching : SearchState()
    data class Results(val cities: List<City>) : SearchState()
    data class Error(val message: String) : SearchState()
}

// States for favorites
sealed class FavoritesState {
    object Idle : FavoritesState()
    object Toggling : FavoritesState()
    data class Error(val message: String) : FavoritesState()
}

// States for online mode
sealed class OnlineModeState {
    object Idle : OnlineModeState()
    object Toggling : OnlineModeState()
    data class Error(val message: String) : OnlineModeState()
}

// States for pagination
data class PaginationState(
    val currentPage: Int = 1,
    val hasMoreData: Boolean = false,
    val isLoadingMore: Boolean = false
)

// States for navigation
sealed class NavigationState {
    object Idle : NavigationState()
    object Navigating : NavigationState()
}

// Application filters
data class CityFilters(
    val showOnlyFavorites: Boolean = false,
    val searchQuery: String = ""
)

// Combined main state
data class CityListUiState(
    val listState: CityListState = CityListState.Loading,
    val searchState: SearchState = SearchState.Idle,
    val favoritesState: FavoritesState = FavoritesState.Idle,
    val onlineModeState: OnlineModeState = OnlineModeState.Idle,
    val navigationState: NavigationState = NavigationState.Idle,
    val paginationState: PaginationState = PaginationState(),
    val filters: CityFilters = CityFilters(),
    val isOnlineMode: Boolean = false
)

// Computed UI state for the screen
data class CityListScreenUiState(
    val isLoading: Boolean = false,
    val cities: List<City> = emptyList(),
    val filteredCities: List<City> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val showOnlyFavorites: Boolean = false,
    val isTogglingFavorites: Boolean = false,
    val isTogglingOnlineMode: Boolean = false,
    val isOnlineMode: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = false,
    val isNavigatingToDetail: Boolean = false
)
