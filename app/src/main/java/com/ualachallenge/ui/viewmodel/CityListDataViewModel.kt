package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.City
import com.domain.models.Result
import com.domain.usecases.LoadAllCitiesUseCase
import com.ualachallenge.ui.viewmodel.states.CityListState
import com.ualachallenge.ui.viewmodel.states.PaginationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CityListDataViewModel @Inject constructor(
    private val loadAllCitiesUseCase: LoadAllCitiesUseCase
) : ViewModel() {

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    private val _listState = MutableStateFlow<CityListState>(CityListState.Loading)
    val listState: StateFlow<CityListState> = _listState.asStateFlow()

    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    private var loadMoreJob: Job? = null
    private var isLoadingAllCities = false

    init {
        loadCities()
    }

    fun loadCities() {
        // Don't show loading state immediately if we're in migration
        // This allows the UI to be interactive during cache migration
        loadCitiesData(page = 1, isRefresh = false, showLoadingImmediately = false)
    }

    // New method to reload data when migration completes
    fun reloadDataAfterMigration() {
        loadCitiesData(page = 1, isRefresh = true, showLoadingImmediately = true)
    }

    fun loadMoreCities() {
        val currentState = paginationState.value
        if (currentState.isLoadingMore || !currentState.hasMoreData || isLoadingAllCities) {
            return
        }

        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            loadCitiesData(page = currentState.currentPage + 1, isRefresh = false, showLoadingImmediately = true)
        }
    }

    fun loadAllCitiesForFavorites() {
        if (isLoadingAllCities) return

        isLoadingAllCities = true
        _listState.update { CityListState.Loading }
        _paginationState.update { it.copy(isLoadingMore = false, hasMoreData = false) }

        viewModelScope.launch {
            try {
                // Load all available cities (using a high limit)
                when (val result = loadAllCitiesUseCase(page = 1)) {
                    is Result.Success -> {
                        _cities.update { result.data }
                        _listState.update { CityListState.Success(result.data) }
                        _paginationState.update {
                            it.copy(
                                currentPage = 1,
                                hasMoreData = false,
                                isLoadingMore = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _listState.update { CityListState.Error(result.message) }
                    }
                    is Result.Loading -> {
                        _listState.update { CityListState.Loading }
                    }
                }
            } finally {
                isLoadingAllCities = false
            }
        }
    }

    fun loadCitiesData(page: Int, isRefresh: Boolean, showLoadingImmediately: Boolean = true) {
        viewModelScope.launch {
            if (showLoadingImmediately) {
                updateLoadingState(page, isRefresh)
            }

            when (val result = loadAllCitiesUseCase(page = page)) {
                is Result.Success -> {
                    handleSuccess(result.data, page, isRefresh)
                }
                is Result.Error -> {
                    handleError(result.message)
                }
                is Result.Loading -> {
                    updateLoadingState(page, isRefresh)
                }
            }
        }
    }

    private fun updateLoadingState(page: Int, isRefresh: Boolean) {
        _listState.update {
            if (page == 1 || isRefresh) CityListState.Loading else it
        }
        _paginationState.update {
            it.copy(
                isLoadingMore = page > 1,
                currentPage = if (page == 1) 1 else it.currentPage
            )
        }
    }

    private fun handleSuccess(newCities: List<City>, page: Int, isRefresh: Boolean) {
        val allCities = if (page == 1 || isRefresh) {
            newCities
        } else {
            _cities.value + newCities
        }

        _cities.update { allCities }

        // If we got empty results and this is the first page, it might be a network issue
        if (allCities.isEmpty() && page == 1) {
        }

        _listState.update { CityListState.Success(allCities) }

        _paginationState.update {
            it.copy(
                currentPage = if (page == 1) 1 else it.currentPage,
                hasMoreData = newCities.isNotEmpty(),
                isLoadingMore = false
            )
        }

        // Notify that data loading is complete (for overlay synchronization)
        onDataLoadingComplete?.invoke()
    }

    private fun handleError(errorMessage: String) {
        _listState.update { CityListState.Error(errorMessage) }
        _paginationState.update { it.copy(isLoadingMore = false) }

        // Notify that data loading is complete (for overlay synchronization)
        onDataLoadingComplete?.invoke()
    }

    // Callback for data loading completion
    private var onDataLoadingComplete: (() -> Unit)? = null

    fun setDataLoadingCompleteCallback(callback: () -> Unit) {
        onDataLoadingComplete = callback
    }

    fun updateCityFavoriteStatus(cityId: Int, isFavorite: Boolean) {
        _cities.update { currentCities ->
            currentCities.map { city ->
                if (city.id == cityId) city.copy(isFavorite = isFavorite) else city
            }
        }

        val currentListState = _listState.value
        if (currentListState is CityListState.Success) {
            _listState.update {
                CityListState.Success(_cities.value)
            }
        }
    }
}
