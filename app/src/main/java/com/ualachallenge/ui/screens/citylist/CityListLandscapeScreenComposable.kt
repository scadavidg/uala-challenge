package com.ualachallenge.ui.screens.citylist

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
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
import com.domain.models.City
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ualachallenge.ui.components.CacheMigrationIndicator
import com.ualachallenge.ui.components.CacheMigrationMessageComposable
import com.ualachallenge.ui.components.CityListComposable
import com.ualachallenge.ui.components.EmptyStateScreenComposable
import com.ualachallenge.ui.components.ErrorScreenComposable
import com.ualachallenge.ui.components.LoadingScreenComposable
import com.ualachallenge.ui.components.MapPlaceholderComposable
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
fun CityListLandscapeScreenComposable(onCityClick: (Int) -> Unit, onMapClick: (Int) -> Unit = {}, selectedCityId: Int? = null) {
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

    // Selected city for map display
    val selectedCityState = remember { mutableStateOf<City?>(null) }

    // LazyListState for scrolling to selected city
    val lazyListState = rememberLazyListState()

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

    // Set the selected city based on selectedCityId or first city as default
    LaunchedEffect(uiState.filteredCities, selectedCityId) {
        if (uiState.filteredCities.isNotEmpty()) {
            if (selectedCityId != null) {
                // Try to find the city with the selected ID
                val city = uiState.filteredCities.find { it.id == selectedCityId }
                if (city != null) {
                    selectedCityState.value = city
                } else {
                    // If city not found, select the first city
                    selectedCityState.value = uiState.filteredCities.first()
                }
            } else if (selectedCityState.value == null) {
                // No selectedCityId provided, select the first city
                selectedCityState.value = uiState.filteredCities.first()
            }
        }
    }

    // Ensure online mode is always available
    LaunchedEffect(Unit) {
        onlineModeViewModel.loadOnlineMode()
    }

    // Load data only if we don't have data yet
    LaunchedEffect(Unit) {
        if (uiState.cities.isEmpty() && !uiState.isLoading) {
            dataViewModel.loadCities()
            favoritesViewModel.loadFavoriteCities()
            // Start migration in background without blocking UI
            migrationViewModel.startMigration()
        }
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
        // Only reload if we're switching to online mode and have no cities
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
                    actions = {
                        // Online/Offline mode toggle - always enabled
                        IconButton(
                            onClick = { onlineModeViewModel.toggleOnlineMode() },
                            enabled = true
                        ) {
                            if (uiState.isTogglingOnlineMode) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 1.5.dp
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
                            enabled = true
                        ) {
                            if (uiState.isTogglingFavorites) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 1.5.dp
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left side - City List (40% width as per wireframe)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.4f)
                        .padding(end = 8.dp)
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

                    // City List Content
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
                                        // Update selected city for map display
                                        val city = uiState.filteredCities.find { it.id == cityId }
                                        if (city != null) {
                                            selectedCityState.value = city
                                        }
                                        onCityClick(cityId)
                                    },
                                    onFavoriteToggle = { cityId -> favoritesViewModel.toggleFavorite(cityId) },
                                    onMapClick = { cityId ->
                                        // Update selected city for map display
                                        val city = uiState.filteredCities.find { it.id == cityId }
                                        selectedCityState.value = city
                                        onMapClick(cityId)
                                    },
                                    isLoadingMore = uiState.isLoadingMore,
                                    hasMoreData = uiState.hasMoreData,
                                    onLoadMore = { dataViewModel.loadMoreCities() },
                                    selectedCityId = selectedCityState.value?.id,
                                    listState = lazyListState
                                )
                            } else {
                                // Show no search results message
                                if (uiState.showOnlyFavorites) {
                                    EmptyStateScreenComposable(isFavorites = true)
                                } else {
                                    EmptyStateScreenComposable(
                                        isOnlineMode = uiState.isOnlineMode,
                                        isSearchResults = true
                                    )
                                }
                            }
                        }

                        // Show main list or empty state
                        else -> {
                            if (uiState.filteredCities.isNotEmpty()) {
                                CityListComposable(
                                    cities = uiState.filteredCities,
                                    onCityClick = { cityId ->
                                        // Update selected city for map display
                                        val city = uiState.filteredCities.find { it.id == cityId }
                                        if (city != null) {
                                            selectedCityState.value = city
                                        }
                                        onCityClick(cityId)
                                    },
                                    onFavoriteToggle = { cityId -> favoritesViewModel.toggleFavorite(cityId) },
                                    onMapClick = { cityId ->
                                        // Update selected city for map display
                                        val city = uiState.filteredCities.find { it.id == cityId }
                                        selectedCityState.value = city
                                        onMapClick(cityId)
                                    },
                                    isLoadingMore = uiState.isLoadingMore,
                                    hasMoreData = uiState.hasMoreData,
                                    onLoadMore = { dataViewModel.loadMoreCities() },
                                    selectedCityId = selectedCityState.value?.id,
                                    listState = lazyListState
                                )
                            } else {
                                // Check if we're in favorites mode and show appropriate message
                                if (uiState.showOnlyFavorites) {
                                    EmptyStateScreenComposable(isFavorites = true)
                                } else {
                                    EmptyStateScreenComposable(isOnlineMode = uiState.isOnlineMode)
                                }
                            }
                        }
                    }
                }

                // Right side - Map View (60% width as per wireframe)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.6f)
                        .padding(start = 8.dp)
                ) {
                    selectedCityState.value?.let { city ->
                        // Map with selected city - force recreation with key
                        val cameraPositionState = rememberCameraPositionState(
                            key = city.id.toString()
                        ) {
                            position = CameraPosition.fromLatLngZoom(
                                LatLng(city.lat, city.lon),
                                12f
                            )
                        }

                        // Force camera animation when city changes
                        LaunchedEffect(city.id) {
                            // Force camera to move to the new city position
                            cameraPositionState.animate(
                                update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(
                                        LatLng(city.lat, city.lon),
                                        12f
                                    )
                                ),
                                durationMs = 1000
                            )
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = false),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                myLocationButtonEnabled = false,
                                mapToolbarEnabled = false
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = LatLng(city.lat, city.lon)),
                                title = city.name,
                                snippet = "${city.name}, ${city.country}"
                            )
                        }
                    } ?: run {
                        // Show placeholder when no city is selected
                        MapPlaceholderComposable()
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

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun CityListLandscapeScreenComposableLightModePreview() {
    MaterialTheme {
        CityListLandscapeScreenComposable(
            onCityClick = {},
            onMapClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CityListLandscapeScreenComposableDarkModePreview() {
    MaterialTheme {
        CityListLandscapeScreenComposable(
            onCityClick = {},
            onMapClick = {}
        )
    }
}
