package com.ualachallenge.ui.viewmodel

import com.domain.models.City
import com.ualachallenge.ui.viewmodel.states.CityFilters
import com.ualachallenge.ui.viewmodel.states.CityListScreenUiState
import com.ualachallenge.ui.viewmodel.states.CityListState
import com.ualachallenge.ui.viewmodel.states.FavoritesState
import com.ualachallenge.ui.viewmodel.states.OnlineModeState
import com.ualachallenge.ui.viewmodel.states.PaginationState
import com.ualachallenge.ui.viewmodel.states.SearchState

class CityListCoordinatorViewModel {
    fun combineStates(
        listState: CityListState,
        paginationState: PaginationState,
        searchState: SearchState,
        favoritesState: FavoritesState,
        onlineModeState: OnlineModeState,
        filters: CityFilters,
        isOnlineMode: Boolean,
        favoriteCities: List<City> = emptyList(),
        isMigrationInProgress: Boolean = false
    ): CityListScreenUiState {
        // Extract cities from states
        val cities = when (listState) {
            is CityListState.Success -> listState.cities
            else -> emptyList()
        }

        val searchResults = when (searchState) {
            is SearchState.Results -> searchState.cities
            else -> emptyList()
        }

        // Determine filtered cities
        val filteredCities = when {
            // If there's an active search query, only show search results
            filters.searchQuery.isNotBlank() -> {
                if (searchResults.isNotEmpty()) {
                    if (filters.showOnlyFavorites) {
                        // If we are in favorites mode and there are search results,
                        // ensure all cities have isFavorite = true
                        searchResults.map { it.copy(isFavorite = true) }
                    } else {
                        searchResults
                    }
                } else {
                    // If search query is active but no results, show empty list
                    emptyList()
                }
            }
            filters.showOnlyFavorites -> favoriteCities.map { it.copy(isFavorite = true) }
            else -> cities
        }

        // Determine loading state - don't show loading if migration is in progress and no cities
        val isLoading = listState is CityListState.Loading && !(isMigrationInProgress && cities.isEmpty())

        // Determine error
        val error = when (listState) {
            is CityListState.Error -> listState.message
            else -> null
        }

        // Determine search state
        val isSearching = searchState is SearchState.Searching

        // Determine toggle states
        val isTogglingFavorites = favoritesState is FavoritesState.Toggling
        val isTogglingOnlineMode = onlineModeState is OnlineModeState.Toggling

        // For favorites, disable infinite scroll since they are obtained directly
        val shouldDisableLoadMore = filters.showOnlyFavorites || searchResults.isNotEmpty()
        val hasMoreData = if (shouldDisableLoadMore) false else paginationState.hasMoreData
        val isLoadingMore = if (shouldDisableLoadMore) false else paginationState.isLoadingMore

        return CityListScreenUiState(
            isLoading = isLoading,
            cities = cities,
            filteredCities = filteredCities,
            error = error,
            searchQuery = filters.searchQuery,
            isSearching = isSearching,
            showOnlyFavorites = filters.showOnlyFavorites,
            isTogglingFavorites = isTogglingFavorites,
            isTogglingOnlineMode = isTogglingOnlineMode,
            isOnlineMode = isOnlineMode,
            isLoadingMore = isLoadingMore,
            hasMoreData = hasMoreData,
            isNavigatingToDetail = false
        )
    }
}
