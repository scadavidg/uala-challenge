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
import com.ualachallenge.ui.viewmodel.CityListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CityListScreenComposable(onCityClick: (Int) -> Unit, onMapClick: (Int) -> Unit = {}, viewModel: CityListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh cities when the screen becomes active
    LaunchedEffect(Unit) {
        viewModel.loadCities()
    }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = uiState.isLoading,
            onRefresh = {
                viewModel.loadCities()
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
                            onClick = { viewModel.toggleShowOnlyFavorites() },
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
                        onClick = { viewModel.toggleOnlineMode() },
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
                        onClick = { viewModel.toggleShowOnlyFavorites() },
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
                                tint = if (uiState.showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        viewModel.searchCities(query)
                    },
                    onTrailingIconClick = {
                        viewModel.searchCities("")
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
                            onRetry = { viewModel.loadCities() }
                        )
                    }

                    else -> {
                        if (uiState.filteredCities.isEmpty()) {
                            EmptyStateScreenComposable()
                        } else {
                            CityListComposable(
                                cities = uiState.filteredCities,
                                onCityClick = { cityId ->
                                    viewModel.navigateToCityDetail(cityId)
                                    onCityClick(cityId)
                                },
                                onFavoriteToggle = { cityId -> viewModel.toggleFavorite(cityId) },
                                onMapClick = onMapClick,
                                isLoadingMore = uiState.isLoadingMore,
                                hasMoreData = uiState.hasMoreData,
                                onLoadMore = { viewModel.loadMoreCities() }
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

            // Overlay de carga para navegación al detalle
            if (uiState.isNavigatingToDetail) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
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
