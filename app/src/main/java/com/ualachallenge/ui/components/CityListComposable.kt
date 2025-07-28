package com.ualachallenge.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onMapClick: (Int) -> Unit = {}
) {
    val groupedCities = cities.groupBy { it.name.first().uppercase() }
        .toSortedMap()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
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
                    onMapClick = { onMapClick(city.id) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityListComposablePreview() {
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
