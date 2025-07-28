package com.ualachallenge.ui.screens.citylist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ualachallenge.ui.components.CityListComposable
import com.ualachallenge.ui.components.EmptyStateScreenComposable
import com.ualachallenge.ui.components.ErrorScreenComposable
import com.ualachallenge.ui.components.LoadingScreenComposable
import com.ualachallenge.ui.viewmodel.CityListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CityListScreenComposable(onCityClick: (Int) -> Unit, viewModel: CityListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = uiState.isLoading,
            onRefresh = { viewModel.loadCities() }
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cities") },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleShowOnlyFavorites() }
                    ) {
                        Icon(
                            imageVector = if (uiState.showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (uiState.showOnlyFavorites) "Show all cities" else "Show only favorites",
                            tint = if (uiState.showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            // Content
            when {
                uiState.isLoading && uiState.cities.isEmpty() -> {
                    LoadingScreenComposable()
                }

                uiState.error != null && uiState.cities.isEmpty() -> {
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
                            onCityClick = onCityClick,
                            onFavoriteToggle = { cityId -> viewModel.toggleFavorite(cityId) }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityListScreenComposablePreview() {
    MaterialTheme {
        CityListScreenComposable(
            onCityClick = {}
        )
    }
}
