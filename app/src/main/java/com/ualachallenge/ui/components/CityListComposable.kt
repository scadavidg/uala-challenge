package com.ualachallenge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domain.models.City

@Composable
fun CityListComposable(
    cities: List<City>,
    onCityClick: (Int) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    onMapClick: (Int) -> Unit = {},
    isLoadingMore: Boolean = false,
    hasMoreData: Boolean = false,
    onLoadMore: (() -> Unit)? = null,
    selectedCityId: Int? = null,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState()
) {
    val groupedCities = cities.groupBy { it.name.first().uppercase() }
        .toSortedMap()

    // Detect when user reaches the end of the list
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount

            lastVisibleItem != null &&
                lastVisibleItem.index >= totalItems - 3 && // Load more when 3 items away from end
                hasMoreData &&
                !isLoadingMore
        }
    }

    // Trigger load more when needed
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && onLoadMore != null) {
            onLoadMore()
        }
    }

    // Scroll to selected city when it changes
    LaunchedEffect(selectedCityId, cities) {
        if (selectedCityId != null && cities.isNotEmpty()) {
            val selectedCity = cities.find { it.id == selectedCityId }
            if (selectedCity != null) {
                val cityIndex = cities.indexOf(selectedCity)
                if (cityIndex >= 0) {
                    // Calculate the position in the grouped list
                    var currentIndex = 0
                    var targetIndex = -1

                    groupedCities.forEach { (_, citiesInGroup) ->
                        if (targetIndex == -1) {
                            // Check if our city is in this group
                            val cityInGroupIndex = citiesInGroup.indexOf(selectedCity)
                            if (cityInGroupIndex >= 0) {
                                targetIndex = currentIndex + cityInGroupIndex + 1 // +1 for the letter header
                            } else {
                                currentIndex += citiesInGroup.size + 1 // +1 for the letter header
                            }
                        }
                    }

                    if (targetIndex >= 0) {
                        listState.animateScrollToItem(targetIndex)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(0.95f) // Leave space for fast scroll
        ) {
            groupedCities.forEach { (letter, citiesInGroup) ->
                item {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(citiesInGroup) { city ->
                    CityItemComposable(
                        city = city,
                        onClick = { onCityClick(city.id) },
                        onFavoriteToggle = { onFavoriteToggle(city.id) },
                        onMapClick = { onMapClick(city.id) },
                        isSelected = selectedCityId == city.id
                    )
                }
            }

            // Show loading indicator at the bottom when loading more
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp), // Reduced padding
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp), // Smaller size
                            strokeWidth = 2.dp // Thinner stroke
                        )
                    }
                }
            }
        }

        // Fast scroll indicator
        FastScrollComposable(
            cities = cities,
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun CityListComposableLightModePreview() {
    MaterialTheme {
        CityListComposable(
            cities = listOf(
                City(
                    id = 1,
                    name = "New York",
                    country = "United States",
                    lat = 40.7128,
                    lon = -74.0060,
                    isFavorite = true
                ),
                City(
                    id = 2,
                    name = "London",
                    country = "United Kingdom",
                    lat = 51.5074,
                    lon = -0.1278,
                    isFavorite = false
                ),
                City(
                    id = 3,
                    name = "Paris",
                    country = "France",
                    lat = 48.8566,
                    lon = 2.3522,
                    isFavorite = true
                )
            ),
            onCityClick = {},
            onFavoriteToggle = {},
            onMapClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun CityListComposableDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        CityListComposable(
            cities = listOf(
                City(
                    id = 1,
                    name = "New York",
                    country = "United States",
                    lat = 40.7128,
                    lon = -74.0060,
                    isFavorite = true
                ),
                City(
                    id = 2,
                    name = "London",
                    country = "United Kingdom",
                    lat = 51.5074,
                    lon = -0.1278,
                    isFavorite = false
                ),
                City(
                    id = 3,
                    name = "Paris",
                    country = "France",
                    lat = 48.8566,
                    lon = 2.3522,
                    isFavorite = true
                )
            ),
            onCityClick = {},
            onFavoriteToggle = {},
            onMapClick = {}
        )
    }
}
