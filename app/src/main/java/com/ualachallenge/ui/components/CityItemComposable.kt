package com.ualachallenge.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domain.models.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityItemComposable(
    city: City,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onMapClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title: City and Country
                Text(
                    text = "${city.name}, ${city.country}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Subtitle: Coordinates
                Text(
                    text = "📍 ${String.format("%.4f", city.lat)}, ${String.format("%.4f", city.lon)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite button with actual logic
                IconButton(
                    onClick = onFavoriteToggle
                ) {
                    Icon(
                        imageVector = if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (city.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (city.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Map navigation button
                IconButton(
                    onClick = onMapClick
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Open map",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode - Favorite")
@Composable
fun CityItemComposableLightModeFavoritePreview() {
    MaterialTheme {
        CityItemComposable(
            city = City(
                id = 1,
                name = "New York",
                country = "United States",
                lat = 40.7128,
                lon = -74.0060,
                isFavorite = true
            ),
            onClick = {},
            onFavoriteToggle = {},
            onMapClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Not Favorite")
@Composable
fun CityItemComposableLightModeNotFavoritePreview() {
    MaterialTheme {
        CityItemComposable(
            city = City(
                id = 2,
                name = "London",
                country = "United Kingdom",
                lat = 51.5074,
                lon = -0.1278,
                isFavorite = false
            ),
            onClick = {},
            onFavoriteToggle = {},
            onMapClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Selected")
@Composable
fun CityItemComposableLightModeSelectedPreview() {
    MaterialTheme {
        CityItemComposable(
            city = City(
                id = 3,
                name = "Paris",
                country = "France",
                lat = 48.8566,
                lon = 2.3522,
                isFavorite = true
            ),
            onClick = {},
            onFavoriteToggle = {},
            onMapClick = {},
            isSelected = true
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CityItemComposableDarkModePreview() {
    MaterialTheme {
        CityItemComposable(
            city = City(
                id = 4,
                name = "Tokyo",
                country = "Japan",
                lat = 35.6762,
                lon = 139.6503,
                isFavorite = false
            ),
            onClick = {},
            onFavoriteToggle = {},
            onMapClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Selected Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CityItemComposableSelectedDarkModePreview() {
    MaterialTheme {
        CityItemComposable(
            city = City(
                id = 5,
                name = "Berlin",
                country = "Germany",
                lat = 52.5200,
                lon = 13.4050,
                isFavorite = true
            ),
            onClick = {},
            onFavoriteToggle = {},
            onMapClick = {},
            isSelected = true
        )
    }
}
