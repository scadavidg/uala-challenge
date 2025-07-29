package com.ualachallenge.ui.screens.citylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ualachallenge.ui.components.CityListComposable
import com.ualachallenge.ui.components.EmptyStateScreenComposable
import com.ualachallenge.ui.components.ErrorScreenComposable
import com.ualachallenge.ui.components.LoadingOverlayComposable
import com.ualachallenge.ui.components.LoadingScreenComposable
import com.ualachallenge.ui.components.OnlineModeIndicatorComposable
import com.ualachallenge.ui.components.SearchBarComposable
import com.ualachallenge.ui.viewmodel.CityFavoritesViewModel
import com.ualachallenge.ui.viewmodel.CityListCoordinatorViewModel
import com.ualachallenge.ui.viewmodel.CityListDataViewModel
import com.ualachallenge.ui.viewmodel.CityOnlineModeViewModel
import com.ualachallenge.ui.viewmodel.CitySearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CityListScreenComposable(onCityClick: (Int) -> Unit, onMapClick: (Int) -> Unit = {}) {
    val dataViewModel: CityListDataViewModel = hiltViewModel()
    val searchViewModel: CitySearchViewModel = hiltViewModel()
    val favoritesViewModel: CityFavoritesViewModel = hiltViewModel()
    val onlineModeViewModel: CityOnlineModeViewModel = hiltViewModel()
    val coordinator = CityListCoordinatorViewModel()

    // Conectar los ViewModels
    LaunchedEffect(dataViewModel) {
        favoritesViewModel.setDataViewModel(dataViewModel)
    }

    LaunchedEffect(searchViewModel) {
        favoritesViewModel.setSearchViewModel(searchViewModel)
    }

    val listState by dataViewModel.listState.collectAsState()
    val paginationState by dataViewModel.paginationState.collectAsState()
    val searchState by searchViewModel.searchState.collectAsState()
    val favoritesState by favoritesViewModel.favoritesState.collectAsState()
    val favoriteCities by favoritesViewModel.favoriteCities.collectAsState()
    val onlineModeState by onlineModeViewModel.onlineModeState.collectAsState()
    val isOnlineMode by onlineModeViewModel.isOnlineMode.collectAsState()

    // Local and mutable states for filters
    val filtersState = remember { mutableStateOf(com.ualachallenge.ui.viewmodel.states.CityFilters()) }
    val filters = filtersState.value

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

    // Refresh cities when the screen becomes active
    LaunchedEffect(Unit) {
        dataViewModel.loadCities()
        favoritesViewModel.loadFavoriteCities()
    }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = uiState.isLoading,
            onRefresh = {
                dataViewModel.loadCities()
                favoritesViewModel.loadFavoriteCities()
            }
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Cities")
                        OnlineModeIndicatorComposable(isOnlineMode = uiState.isOnlineMode)
                    }
                },
                navigationIcon = {
                    if (uiState.showOnlyFavorites) {
                        IconButton(
                            onClick = {
                                filtersState.value = filters.copy(showOnlyFavorites = false, searchQuery = "")
                                // Clear the search
                                searchViewModel.clearSearch()
                                // Return to normal pagination
                                dataViewModel.loadCities()
                            },
                            enabled = !uiState.isTogglingFavorites
                        ) {
                            if (uiState.isTogglingFavorites) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to all cities"
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Online/Offline mode toggle
                    IconButton(
                        onClick = { onlineModeViewModel.toggleOnlineMode() },
                        enabled = !uiState.isTogglingOnlineMode
                    ) {
                        if (uiState.isTogglingOnlineMode) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (uiState.isOnlineMode) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = if (uiState.isOnlineMode) "Switch to offline mode" else "Switch to online mode",
                                tint = if (uiState.isOnlineMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Favorites toggle
                    IconButton(
                        onClick = {
                            val newShowOnlyFavorites = !uiState.showOnlyFavorites
                            filtersState.value = filters.copy(showOnlyFavorites = newShowOnlyFavorites, searchQuery = "")

                            // Clear the search
                            searchViewModel.clearSearch()

                            // If the favorites filter is activated, load all cities
                            if (newShowOnlyFavorites) {
                                dataViewModel.loadAllCitiesForFavorites()
                            }
                        },
                        enabled = !uiState.isTogglingFavorites
                    ) {
                        if (uiState.isTogglingFavorites) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (uiState.showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (uiState.showOnlyFavorites) "Show all cities" else "Show only favorites",
                                tint = if (uiState.showOnlyFavorites) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                SearchBarComposable(
                    query = uiState.searchQuery,
                    onQueryChange = { query ->
                        filtersState.value = filters.copy(searchQuery = query)
                        searchViewModel.searchCities(query, uiState.showOnlyFavorites)
                    },
                    onTrailingIconClick = {
                        filtersState.value = filters.copy(searchQuery = "")
                        searchViewModel.searchCities("", uiState.showOnlyFavorites)
                    },
                    isSearching = uiState.isSearching
                )

                // Content
                when {
                    uiState.isLoading && uiState.cities.isEmpty() -> {
                        LoadingScreenComposable()
                    }

                    uiState.error != null && uiState.cities.isEmpty() && uiState.filteredCities.isEmpty() -> {
                        ErrorScreenComposable(
                            error = uiState.error!!,
                            onRetry = { dataViewModel.loadCities() }
                        )
                    }

                    else -> {
                        if (uiState.filteredCities.isEmpty()) {
                            EmptyStateScreenComposable()
                        } else {
                            CityListComposable(
                                cities = uiState.filteredCities,
                                onCityClick = { cityId ->
                                    onCityClick(cityId)
                                },
                                onFavoriteToggle = { cityId -> favoritesViewModel.toggleFavorite(cityId) },
                                onMapClick = onMapClick,
                                isLoadingMore = uiState.isLoadingMore,
                                hasMoreData = uiState.hasMoreData,
                                onLoadMore = { dataViewModel.loadMoreCities() }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Overlay de carga de pantalla completa - solo mostrar para carga inicial
            if (uiState.isLoading && uiState.cities.isEmpty()) {
                LoadingOverlayComposable()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityListScreenComposablePreview() {
    MaterialTheme {
        CityListScreenComposable(
            onCityClick = {},
            onMapClick = {}
        )
    }
}
