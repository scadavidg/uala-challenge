package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.SearchCitiesUseCase
import com.ualachallenge.ui.viewmodel.states.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val searchCitiesUseCase: SearchCitiesUseCase
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null
    private var lastSearchQuery = ""
    private var lastOnlyFavorites = false

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 300L
        private const val MAX_SEARCH_QUERY_LENGTH = 50
    }

    fun searchCities(query: String, onlyFavorites: Boolean = false) {
        val sanitizedQuery = query.trim()
        _searchQuery.update { sanitizedQuery }

        // Save the parameters of the last search
        lastSearchQuery = sanitizedQuery
        lastOnlyFavorites = onlyFavorites

        searchJob?.cancel()
        _searchState.update { SearchState.Idle }

        searchJob = viewModelScope.launch {
            try {
                delay(SEARCH_DEBOUNCE_DELAY)

                if (sanitizedQuery.isBlank()) {
                    handleEmptySearch()
                    return@launch
                }

                if (sanitizedQuery.length > MAX_SEARCH_QUERY_LENGTH) {
                    handleSearchError("Search query too long")
                    return@launch
                }

                _searchState.update { SearchState.Searching }

                when (val result = searchCitiesUseCase(sanitizedQuery, onlyFavorites)) {
                    is Result.Success -> {
                        handleSearchSuccess(result.data)
                    }
                    is Result.Error -> {
                        handleSearchError(result.message)
                    }
                    is Result.Loading -> {
                        _searchState.update { SearchState.Searching }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException || e.cause is kotlinx.coroutines.CancellationException) {
                    return@launch
                }
                handleSearchError("Search failed: ${e.message}")
            }
        }
    }

    // Method to refresh the current search when favorites change
    fun refreshCurrentSearch() {
        if (lastSearchQuery.isNotBlank()) {
            // Don't change state immediately to avoid flickering
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                try {
                    // Small delay to avoid multiple rapid searches
                    delay(100)

                    when (val result = searchCitiesUseCase(lastSearchQuery, lastOnlyFavorites)) {
                        is Result.Success -> {
                            handleSearchSuccess(result.data)
                        }
                        is Result.Error -> {
                            handleSearchError(result.message)
                        }
                        is Result.Loading -> {
                            _searchState.update { SearchState.Searching }
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException || e.cause is kotlinx.coroutines.CancellationException) {
                        return@launch
                    }
                    handleSearchError("Search refresh failed: ${e.message}")
                }
            }
        }
    }

    private fun handleEmptySearch() {
        _searchState.update { SearchState.Idle }
    }

    private fun handleSearchSuccess(cities: List<City>) {
        _searchState.update { SearchState.Results(cities) }
    }

    private fun handleSearchError(errorMessage: String) {
        _searchState.update { SearchState.Error(errorMessage) }
    }

    fun clearSearch() {
        _searchQuery.update { "" }
        _searchState.update { SearchState.Idle }
        lastSearchQuery = ""
        lastOnlyFavorites = false
    }

    fun getCurrentQuery(): String = _searchQuery.value

    fun isSearching(): Boolean = _searchState.value is SearchState.Searching

    fun getSearchResults(): List<City> {
        return when (val state = _searchState.value) {
            is SearchState.Results -> state.cities
            else -> emptyList()
        }
    }

    fun getSearchError(): String? {
        return when (val state = _searchState.value) {
            is SearchState.Error -> state.message
            else -> null
        }
    }
}
