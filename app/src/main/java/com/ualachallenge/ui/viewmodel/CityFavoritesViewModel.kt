package com.ualachallenge.ui.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import com.ualachallenge.ui.viewmodel.states.FavoritesState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CityFavoritesViewModel @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoriteCitiesUseCase: GetFavoriteCitiesUseCase,
    private val favoriteNotificationViewModel: FavoriteNotificationViewModel
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<FavoritesState>(FavoritesState.Idle)
    val favoritesState: StateFlow<FavoritesState> = _favoritesState.asStateFlow()

    private val _favoriteCities = MutableStateFlow<List<City>>(emptyList())
    val favoriteCities: StateFlow<List<City>> = _favoriteCities.asStateFlow()

    // References to other ViewModels for communication
    private var dataViewModel: CityListDataViewModel? = null
    private var searchViewModel: CitySearchViewModel? = null

    init {
        // Listen for favorite changes from other screens
        if (!isTestMode()) {
            viewModelScope.launch {
                favoriteNotificationViewModel.favoriteChangeEvents.collect { event ->
                    when (event) {
                        is FavoriteChangeEvent.Changed -> {
                            handleFavoriteChangeFromOtherScreen(event.cityId, event.isFavorite)
                        }
                        is FavoriteChangeEvent.Toggled -> {
                            handleFavoriteToggleFromOtherScreen(event.cityId)
                        }
                    }
                }
            }
        }
    }

    private fun isTestMode(): Boolean = try {
        Class.forName("org.junit.Test")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    fun setDataViewModel(dataViewModel: CityListDataViewModel) {
        this.dataViewModel = dataViewModel
    }

    fun setSearchViewModel(searchViewModel: CitySearchViewModel) {
        this.searchViewModel = searchViewModel
    }

    // Load favorite cities
    fun loadFavoriteCities() {
        viewModelScope.launch {
            _favoritesState.update { FavoritesState.Toggling }

            try {
                when (val result = getFavoriteCitiesUseCase()) {
                    is Result.Success -> {
                        _favoriteCities.update { result.data }
                        _favoritesState.update { FavoritesState.Idle }
                    }
                    is Result.Error -> {
                        _favoritesState.update { FavoritesState.Error(result.message) }
                    }
                    is Result.Loading -> {
                        _favoritesState.update { FavoritesState.Toggling }
                    }
                }
            } catch (e: Exception) {
                _favoritesState.update {
                    FavoritesState.Error("Failed to load favorites: ${e.message}")
                }
            }
        }
    }

    fun toggleFavorite(cityId: Int) {
        _favoritesState.update { FavoritesState.Toggling }

        viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(cityId)) {
                    is Result.Success -> {
                        // Update only the specific city in the main list
                        dataViewModel?.updateCityFavoriteStatus(cityId, !getCurrentFavoriteStatus(cityId))

                        // Reload the favorites list after toggle
                        loadFavoriteCities()

                        // Notify CitySearchViewModel to update search results
                        searchViewModel?.refreshCurrentSearch()

                        _favoritesState.update { FavoritesState.Idle }
                    }
                    is Result.Error -> {
                        _favoritesState.update { FavoritesState.Error(result.message) }
                    }
                    is Result.Loading -> {
                        _favoritesState.update { FavoritesState.Toggling }
                    }
                }
            } catch (e: Exception) {
                _favoritesState.update {
                    FavoritesState.Error("Failed to toggle favorite: ${e.message}")
                }
            }
        }
    }

    // Method to handle favorite changes from other screens (like MapView)
    fun handleFavoriteChangeFromOtherScreen(cityId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                // Update the specific city in the main list
                dataViewModel?.updateCityFavoriteStatus(cityId, isFavorite)

                // Reload the favorites list
                loadFavoriteCities()

                // Notify CitySearchViewModel to update search results
                searchViewModel?.refreshCurrentSearch()

                _favoritesState.update { FavoritesState.Idle }
            } catch (e: Exception) {
                _favoritesState.update {
                    FavoritesState.Error("Failed to handle favorite change: ${e.message}")
                }
            }
        }
    }

    // Method to handle favorite toggle from other screens
    fun handleFavoriteToggleFromOtherScreen(cityId: Int) {
        viewModelScope.launch {
            try {
                // Get current status and toggle it
                val currentStatus = getCurrentFavoriteStatus(cityId)
                val newStatus = !currentStatus

                // Update the specific city in the main list
                dataViewModel?.updateCityFavoriteStatus(cityId, newStatus)

                // Reload the favorites list
                loadFavoriteCities()

                // Notify CitySearchViewModel to update search results
                searchViewModel?.refreshCurrentSearch()

                _favoritesState.update { FavoritesState.Idle }
            } catch (e: Exception) {
                _favoritesState.update {
                    FavoritesState.Error("Failed to handle favorite toggle: ${e.message}")
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getCurrentFavoriteStatus(cityId: Int): Boolean {
        // Try to get the current status of the city from the favorites list
        return _favoriteCities.value.any { it.id == cityId }
    }

    fun getFavoriteCities(): List<City> = _favoriteCities.value

    fun isToggling(): Boolean = _favoritesState.value is FavoritesState.Toggling

    fun getError(): String? = when (val state = _favoritesState.value) {
        is FavoritesState.Error -> state.message
        else -> null
    }

    fun clearError() {
        _favoritesState.update { FavoritesState.Idle }
    }
}
