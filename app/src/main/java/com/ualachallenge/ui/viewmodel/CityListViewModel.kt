package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetOnlineModeUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.SearchCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import com.domain.usecases.ToggleOnlineModeUseCase
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
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val toggleOnlineModeUseCase: ToggleOnlineModeUseCase,
    private val getOnlineModeUseCase: GetOnlineModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityListUiState())
    val uiState: StateFlow<CityListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        loadCities()
        loadOnlineMode()
    }

    // MARK: - Initialization
    private fun loadOnlineMode() {
        viewModelScope.launch {
            when (val result = getOnlineModeUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isOnlineMode = result.data) }
                }
                is Result.Error, is Result.Loading -> {
                    // Keep default value (false)
                }
            }
        }
    }

    // MARK: - Data Loading
    fun loadCities() {
        loadCitiesData(page = 1, isRefresh = false)
    }

    fun refreshCities() {
        loadCitiesData(page = 1, isRefresh = true)
    }

    private fun loadCitiesData(page: Int, isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = isRefresh || page == 1,
                    isLoadingMore = page > 1,
                    error = null,
                    currentPage = if (page == 1) 1 else it.currentPage
                )
            }

            when (val result = loadAllCitiesUseCase(page = page)) {
                is Result.Success -> {
                    updateCitiesState(result.data, page, isRefresh)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update {
                        it.copy(
                            isLoading = page == 1,
                            isLoadingMore = page > 1
                        )
                    }
                }
            }
        }
    }

    private fun updateCitiesState(newCities: List<City>, page: Int, isRefresh: Boolean) {
        _uiState.update { currentState ->
            val allCities = if (page == 1 || isRefresh) {
                newCities
            } else {
                currentState.cities + newCities
            }

            val updatedFilteredCities = if (currentState.searchQuery.isBlank()) {
                applyFilters(allCities, currentState.searchQuery, currentState.showOnlyFavorites)
            } else {
                // Keep current search results but update the underlying cities list
                currentState.filteredCities
            }

            currentState.copy(
                cities = allCities,
                filteredCities = updatedFilteredCities,
                isLoading = false,
                isLoadingMore = false,
                error = null,
                currentPage = page,
                hasMoreData = newCities.isNotEmpty() && newCities.size >= 20
            )
        }
    }

    fun loadMoreCities() {
        val currentState = uiState.value
        if (currentState.isLoadingMore || !currentState.hasMoreData || currentState.isSearching) {
            return
        }

        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            loadCitiesData(page = currentState.currentPage + 1, isRefresh = false)
        }
    }

    // MARK: - Search
    fun searchCities(query: String) {
        val sanitizedQuery = query.trim()
        _uiState.update { it.copy(searchQuery = sanitizedQuery) }

        searchJob?.cancel()
        _uiState.update { it.copy(isSearching = false) }

        searchJob = viewModelScope.launch {
            try {
                delay(300) // 300ms debounce

                if (sanitizedQuery.isBlank()) {
                    handleEmptySearch()
                    return@launch
                }

                _uiState.update { it.copy(isSearching = true, error = null) }

                when (val result = searchCitiesUseCase(sanitizedQuery, uiState.value.showOnlyFavorites)) {
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
                        handleSearchError(sanitizedQuery, result.message)
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isSearching = true) }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException || e.cause is kotlinx.coroutines.CancellationException) {
                    return@launch
                }
                handleSearchError(sanitizedQuery, "Search failed: ${e.message}")
            }
        }
    }

    private fun handleEmptySearch() {
        _uiState.update { currentState ->
            val newFilteredCities = applyFilters(currentState.cities, "", currentState.showOnlyFavorites)
            currentState.copy(
                filteredCities = newFilteredCities,
                isSearching = false,
                error = null
            )
        }
    }

    private fun handleSearchError(query: String, errorMessage: String) {
        val fallbackResults = applyFilters(uiState.value.cities, query, uiState.value.showOnlyFavorites)
        _uiState.update {
            it.copy(
                isSearching = false,
                error = errorMessage,
                filteredCities = fallbackResults
            )
        }
    }

    fun clearSearch() {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = "",
                filteredCities = applyFilters(currentState.cities, "", currentState.showOnlyFavorites),
                error = null
            )
        }
    }

    // MARK: - Favorites
    fun toggleFavorite(cityId: Int) {
        _uiState.update { it.copy(isTogglingFavorites = true, error = null) }

        viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(cityId)) {
                    is Result.Success -> {
                        updateFavoriteStatus(cityId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isTogglingFavorites = false, error = result.message)
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isTogglingFavorites = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isTogglingFavorites = false, error = "Failed to toggle favorite: ${e.message}")
                }
            }
        }
    }

    private fun updateFavoriteStatus(cityId: Int) {
        _uiState.update { currentState ->
            val updatedCities = currentState.cities.map { city ->
                if (city.id == cityId) city.copy(isFavorite = !city.isFavorite) else city
            }

            val updatedFilteredCities = if (currentState.searchQuery.isBlank()) {
                applyFilters(updatedCities, currentState.searchQuery, currentState.showOnlyFavorites)
            } else {
                currentState.filteredCities.map { city ->
                    if (city.id == cityId) city.copy(isFavorite = !city.isFavorite) else city
                }
            }

            currentState.copy(
                cities = updatedCities,
                filteredCities = updatedFilteredCities,
                isTogglingFavorites = false
            )
        }
    }

    fun toggleShowOnlyFavorites() {
        val wasShowingFavorites = uiState.value.showOnlyFavorites
        _uiState.update { it.copy(isTogglingFavorites = true, error = null) }

        viewModelScope.launch {
            try {
                val currentState = uiState.value
                val newShowOnlyFavorites = !currentState.showOnlyFavorites

                val updatedFilteredCities = if (currentState.searchQuery.isBlank()) {
                    applyFilters(currentState.cities, currentState.searchQuery, newShowOnlyFavorites)
                } else {
                    if (newShowOnlyFavorites) {
                        currentState.filteredCities.filter { it.isFavorite }
                    } else {
                        currentState.filteredCities
                    }
                }

                _uiState.update {
                    it.copy(
                        showOnlyFavorites = newShowOnlyFavorites,
                        filteredCities = updatedFilteredCities,
                        isTogglingFavorites = false
                    )
                }

                if (wasShowingFavorites) {
                    refreshCities()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isTogglingFavorites = false, error = "Failed to toggle favorites filter: ${e.message}")
                }
            }
        }
    }

    // MARK: - Online Mode
    fun toggleOnlineMode() {
        val currentMode = uiState.value.isOnlineMode
        _uiState.update { it.copy(isTogglingOnlineMode = true, error = null) }

        viewModelScope.launch {
            try {
                when (val result = toggleOnlineModeUseCase(!currentMode)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isOnlineMode = !currentMode, isTogglingOnlineMode = false) }
                        loadCities()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isTogglingOnlineMode = false, error = result.message)
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isTogglingOnlineMode = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isTogglingOnlineMode = false, error = "Failed to toggle online mode: ${e.message}")
                }
            }
        }
    }

    // MARK: - Navigation
    fun navigateToCityDetail(cityId: Int) {
        _uiState.update { it.copy(isNavigatingToDetail = true, error = null) }

        viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isNavigatingToDetail = false) }
        }
    }

    // MARK: - Utilities
    private fun applyFilters(cities: List<City>, searchQuery: String, showOnlyFavorites: Boolean): List<City> {
        var filteredCities = cities

        if (searchQuery.isNotBlank()) {
            val normalizedQuery = searchQuery.trim().lowercase()
            filteredCities = filteredCities.filter { city ->
                city.name.lowercase().startsWith(normalizedQuery)
            }
        }

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
    val isLoadingMore: Boolean = false,
    val isSearching: Boolean = false,
    val isTogglingOnlineMode: Boolean = false,
    val isTogglingFavorites: Boolean = false,
    val isNavigatingToDetail: Boolean = false,
    val error: String? = null,
    val showOnlyFavorites: Boolean = false,
    val searchQuery: String = "",
    val isOnlineMode: Boolean = false,
    val currentPage: Int = 1,
    val hasMoreData: Boolean = false
)
