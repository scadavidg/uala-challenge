package com.ualachallenge.ui.screens.mapview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
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
import com.ualachallenge.ui.components.ErrorScreenComposable
import com.ualachallenge.ui.components.LoadingScreenComposable
import com.ualachallenge.ui.viewmodel.MapViewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreenComposable(
    onNavigateBack: () -> Unit,
    viewModel: MapViewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map View") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingScreenComposable()
                }
                uiState.error != null -> {
                    ErrorScreenComposable(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadCityDetails() }
                    )
                }
                uiState.city != null -> {
                    MapWithFloatingCard(
                        city = uiState.city!!,
                        onToggleFavorite = { viewModel.toggleFavorite() }
                    )
                }
                else -> {
                    // Placeholder for when city is not available
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Map View",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No city data available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Try navigating from the city list",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapWithFloatingCard(
    city: City,
    onToggleFavorite: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Map takes most of the screen space
        MapContent(city = city)

        // Floating card with city information at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, bottom = 16.dp, end = 100.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            FloatingCityCard(
                city = city,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

@Composable
private fun MapContent(city: City) {
    val cityLatLng = LatLng(city.lat, city.lon)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cityLatLng, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    ) {
        Marker(
            state = MarkerState(position = cityLatLng),
            title = city.name,
            snippet = "${city.name}, ${city.country}"
        )
    }
}

@Composable
private fun FloatingCityCard(
    city: City,
    onToggleFavorite: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(8.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // City header with favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = city.country,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (city.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (city.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coordinates
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = "Coordinates",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Latitude: ${String.format("%.6f", city.lat)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Longitude: ${String.format("%.6f", city.lon)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapViewScreenComposablePreview() {
    MaterialTheme {
        MapViewScreenComposable(
            onNavigateBack = {}
        )
    }
}
