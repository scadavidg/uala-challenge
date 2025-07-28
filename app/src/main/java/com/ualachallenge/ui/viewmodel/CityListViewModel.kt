package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.SearchCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
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
class CityListViewModel @Inject constructor(
    private val loadAllCitiesUseCase: LoadAllCitiesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoriteCitiesUseCase: GetFavoriteCitiesUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityListUiState())
    val uiState: StateFlow<CityListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCities()
    }

    fun loadCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = loadAllCitiesUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            cities = result.data,
                            filteredCities = applyFilters(result.data, it.searchQuery, it.showOnlyFavorites),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun searchCities(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel previous search job
        searchJob?.cancel()

        // Debounce search to avoid too many API calls
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce

            if (query.isBlank()) {
                // If query is empty, show all cities
                _uiState.update { currentState ->
                    currentState.copy(
                        filteredCities = applyFilters(currentState.cities, "", currentState.showOnlyFavorites),
                        isSearching = false
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSearching = true) }

            when (val result = searchCitiesUseCase(query, uiState.value.showOnlyFavorites)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            filteredCities = result.data,
                            isSearching = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isSearching = true) }
                }
            }
        }
    }

    fun toggleFavorite(cityId: Int) {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(cityId)) {
                is Result.Success -> {
                    // Update the cities list with the new favorite status
                    _uiState.update { currentState ->
                        val updatedCities = currentState.cities.map { city ->
                            if (city.id == cityId) city.copy(isFavorite = !city.isFavorite) else city
                        }
                        currentState.copy(
                            cities = updatedCities,
                            filteredCities = applyFilters(updatedCities, currentState.searchQuery, currentState.showOnlyFavorites)
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(error = result.message)
                    }
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun toggleShowOnlyFavorites() {
        val wasShowingFavorites = uiState.value.showOnlyFavorites

        _uiState.update { currentState ->
            val newShowOnlyFavorites = !currentState.showOnlyFavorites
            currentState.copy(
                showOnlyFavorites = newShowOnlyFavorites,
                filteredCities = applyFilters(currentState.cities, currentState.searchQuery, newShowOnlyFavorites)
            )
        }

        // If switching from favorites to all cities, refresh the data
        if (wasShowingFavorites) {
            refreshCities()
        }
    }

    fun refreshCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = loadAllCitiesUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            cities = result.data,
                            filteredCities = applyFilters(result.data, it.searchQuery, it.showOnlyFavorites),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun applyFilters(cities: List<City>, searchQuery: String, showOnlyFavorites: Boolean): List<City> {
        var filteredCities = cities

        // Apply search filter if query is not empty
        if (searchQuery.isNotBlank()) {
            val normalizedQuery = searchQuery.trim().lowercase()
            filteredCities = filteredCities.filter { city ->
                city.name.lowercase().startsWith(normalizedQuery)
            }
        }

        // Apply favorites filter
        if (showOnlyFavorites) {
            filteredCities = filteredCities.filter { it.isFavorite }
        }

        return filteredCities
    }
}

data class CityListUiState(
    val cities: List<City> = emptyList(),
    val filteredCities: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val showOnlyFavorites: Boolean = false,
    val searchQuery: String = ""
)
