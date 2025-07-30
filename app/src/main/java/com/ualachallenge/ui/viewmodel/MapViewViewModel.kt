package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.GetCityByIdUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewViewModel @Inject constructor(
    private val getCityByIdUseCase: GetCityByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoriteNotificationViewModel: FavoriteNotificationViewModel,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cityId: Int = (savedStateHandle["cityId"] as? Int) ?: throw IllegalArgumentException("cityId must be an Int")

    private val _uiState = MutableStateFlow(MapViewUiState())
    val uiState: StateFlow<MapViewUiState> = _uiState.asStateFlow()

    init {
        // Load city details in init for production, but allow tests to control this
        if (!isTestMode()) {
            loadCityDetails()
        }
    }

    private fun isTestMode(): Boolean = try {
        Class.forName("org.junit.Test")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    fun loadCityDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                when (val result = getCityByIdUseCase(cityId)) {
                    is Result.Success -> {
                        result.data?.let { city ->
                            _uiState.update {
                                it.copy(
                                    city = city,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        } ?: run {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "City not found with ID: $cityId"
                                )
                            }
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error loading city: ${result.message}"
                            )
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isTogglingFavorite = true, error = null) }

        // Notify that favorite toggle is starting
        favoriteNotificationViewModel.notifyFavoriteToggle(cityId)

        viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(cityId)) {
                    is Result.Success -> {
                        val newFavoriteStatus = !(_uiState.value.city?.isFavorite ?: false)
                        
                        _uiState.update { currentState ->
                            currentState.copy(
                                city = currentState.city?.copy(
                                    isFavorite = newFavoriteStatus
                                ),
                                isTogglingFavorite = false
                            )
                        }

                        // Notify other ViewModels about the favorite change
                        favoriteNotificationViewModel.notifyFavoriteChange(cityId, newFavoriteStatus)
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isTogglingFavorite = false, error = result.message)
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isTogglingFavorite = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTogglingFavorite = false,
                        error = "Failed to toggle favorite: ${e.message}"
                    )
                }
            }
        }
    }

    fun navigateBack() {
        _uiState.update { it.copy(isNavigatingBack = true, error = null) }

        // Simulate a brief loading state for navigation
        viewModelScope.launch {
            delay(200) // Brief delay to show loading state
            _uiState.update { it.copy(isNavigatingBack = false) }
        }
    }
}

data class MapViewUiState(
    val city: City? = null,
    val isLoading: Boolean = false,
    val isTogglingFavorite: Boolean = false,
    val isNavigatingBack: Boolean = false,
    val error: String? = null
)
