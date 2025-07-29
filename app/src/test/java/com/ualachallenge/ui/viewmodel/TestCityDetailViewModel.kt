package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.Result
import com.domain.usecases.GetCityByIdUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Test version of CityDetailViewModel without Hilt
class TestCityDetailViewModel(
    private val getCityByIdUseCase: GetCityByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cityId: Int = (savedStateHandle["cityId"] as? Int) ?: throw IllegalArgumentException("cityId must be an Int")

    private val _uiState = MutableStateFlow(CityDetailUiState())
    val uiState: StateFlow<CityDetailUiState> = _uiState.asStateFlow()

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

        viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(cityId)) {
                    is Result.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                city = currentState.city?.copy(
                                    isFavorite = !currentState.city.isFavorite
                                ),
                                isTogglingFavorite = false
                            )
                        }
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
}
