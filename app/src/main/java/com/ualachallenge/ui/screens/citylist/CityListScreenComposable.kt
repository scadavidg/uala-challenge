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
import com.ualachallenge.ui.components.CacheMigrationIndicator
import com.ualachallenge.ui.components.CacheMigrationMessageComposable
import com.ualachallenge.ui.components.CityListComposable
import com.ualachallenge.ui.components.EmptyStateScreenComposable
import com.ualachallenge.ui.components.ErrorScreenComposable
import com.ualachallenge.ui.components.LoadingScreenComposable
import com.ualachallenge.ui.components.ModeTransitionOverlay
import com.ualachallenge.ui.components.OnlineModeIndicatorComposable
import com.ualachallenge.ui.components.SearchBarComposable
import com.ualachallenge.ui.viewmodel.CacheMigrationViewModel
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
    val migrationViewModel: CacheMigrationViewModel = hiltViewModel()
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
    val isTransitioning by onlineModeViewModel.isTransitioning.collectAsState()
    val migrationProgress by migrationViewModel.migrationProgress.collectAsState()
    val isMigrationInProgress by migrationViewModel.isMigrationInProgress.collectAsState()
    val migrationCompleted by migrationViewModel.migrationCompleted.collectAsState()

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
        favoriteCities = favoriteCities,
        isMigrationInProgress = isMigrationInProgress
    )

    // Ensure online mode is always available
    LaunchedEffect(Unit) {
        onlineModeViewModel.loadOnlineMode()
    }

    // Load data after online mode is ready
    LaunchedEffect(Unit) {
        dataViewModel.loadCities()
        favoritesViewModel.loadFavoriteCities()
        // Start migration in background without blocking UI
        migrationViewModel.startMigration()
    }

    // Set up data loading completion callback
    LaunchedEffect(dataViewModel) {
        dataViewModel.setDataLoadingCompleteCallback {
            // Hide overlay when data loading is complete
            onlineModeViewModel.hideTransitionOverlay()
        }
    }

    // Listen for migration completion and reload data
    LaunchedEffect(migrationCompleted) {
        if (migrationCompleted) {
            // Reload data after migration completes
            dataViewModel.reloadDataAfterMigration()
            favoritesViewModel.loadFavoriteCities()
            // Reset the completion state
            migrationViewModel.resetMigrationCompleted()
        }
    }

    // Listen for online mode changes and reload data if needed
    LaunchedEffect(isOnlineMode) {
        // If we're in online mode and have no cities, try to load data
        if (isOnlineMode && uiState.cities.isEmpty() && !uiState.isLoading) {
            dataViewModel.loadCitiesData(page = 1, isRefresh = true, showLoadingImmediately = true)
        }
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
            Column {
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
                                        modifier = Modifier.size(20.dp), // Smaller size
                                        strokeWidth = 1.5.dp // Thinner stroke
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
                        // Online/Offline mode toggle - always enabled
                        IconButton(
                            onClick = { onlineModeViewModel.toggleOnlineMode() },
                            enabled = true // Always enabled, regardless of migration status
                        ) {
                            if (uiState.isTogglingOnlineMode) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp), // Smaller size
                                    strokeWidth = 1.5.dp // Thinner stroke
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.isOnlineMode) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = if (uiState.isOnlineMode) "Switch to offline mode" else "Switch to online mode",
                                    tint = if (uiState.isOnlineMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Favorites toggle - always enabled
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
                            enabled = true // Always enabled, regardless of migration status
                        ) {
                            if (uiState.isTogglingFavorites) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp), // Smaller size
                                    strokeWidth = 1.5.dp // Thinner stroke
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

                // Cache Migration Indicator
                CacheMigrationIndicator(
                    progress = migrationProgress,
                    isVisible = isMigrationInProgress
                )
            }
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
                    // Show loading screen only if no cities and not in migration
                    uiState.isLoading && uiState.cities.isEmpty() && !isMigrationInProgress -> {
                        LoadingScreenComposable()
                    }

                    uiState.error != null && uiState.cities.isEmpty() && uiState.filteredCities.isEmpty() -> {
                        ErrorScreenComposable(
                            error = uiState.error!!,
                            onRetry = { dataViewModel.loadCities() }
                        )
                    }

                    // Show cache migration message when in migration and offline mode with no cities
                    isMigrationInProgress && !uiState.isOnlineMode && uiState.cities.isEmpty() && uiState.filteredCities.isEmpty() -> {
                        CacheMigrationMessageComposable()
                    }

                    // Show online mode error when online mode is active but no data
                    uiState.isOnlineMode && uiState.cities.isEmpty() && uiState.filteredCities.isEmpty() && !isMigrationInProgress -> {
                        EmptyStateScreenComposable(isOnlineMode = true)
                    }

                    // Show search results or no results message
                    uiState.searchQuery.isNotBlank() -> {
                        if (uiState.filteredCities.isNotEmpty()) {
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
                        } else {
                            // Show no search results message
                            EmptyStateScreenComposable(
                                isOnlineMode = uiState.isOnlineMode,
                                isSearchResults = true
                            )
                        }
                    }

                    // Show main list or empty state
                    else -> {
                        if (uiState.filteredCities.isNotEmpty()) {
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
                        } else {
                            EmptyStateScreenComposable(isOnlineMode = uiState.isOnlineMode)
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Mode transition overlay
            if (isTransitioning) {
                ModeTransitionOverlay(
                    isOnlineMode = isOnlineMode,
                    modifier = Modifier.fillMaxSize()
                )
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
