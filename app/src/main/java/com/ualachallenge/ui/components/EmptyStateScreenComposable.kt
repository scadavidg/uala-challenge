package com.ualachallenge.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EmptyStateScreenComposable(
    isOnlineMode: Boolean = false,
    isSearchResults: Boolean = false,
    isFavorites: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        // Icon based on the type of empty state
        Icon(
            imageVector = when {
                isSearchResults -> Icons.Default.Search
                isOnlineMode -> Icons.Default.WifiOff
                isFavorites -> Icons.Default.Favorite
                else -> Icons.Default.LocationOff
            },
            contentDescription = when {
                isSearchResults -> "Search icon"
                isOnlineMode -> "No internet connection"
                isFavorites -> "No favorites"
                else -> "No cities available"
            },
            modifier = Modifier.size(64.dp),
            tint = when {
                isSearchResults -> MaterialTheme.colorScheme.primary
                isOnlineMode -> MaterialTheme.colorScheme.error
                isFavorites -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                isSearchResults -> "No results found"
                isOnlineMode -> "Unable to load cities"
                isFavorites -> "No favorites yet"
                else -> "No cities available"
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                isSearchResults -> "Try different search terms"
                isOnlineMode -> "Check your internet connection or try again later"
                isFavorites -> "Add some cities to your favorites!"
                else -> "Check your internet connection"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Default")
@Composable
fun EmptyStateScreenComposableLightModeDefaultPreview() {
    MaterialTheme {
        EmptyStateScreenComposable()
    }
}

@Preview(showBackground = true, name = "Light Mode - Search")
@Composable
fun EmptyStateScreenComposableLightModeSearchPreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isSearchResults = true)
    }
}

@Preview(showBackground = true, name = "Light Mode - Online")
@Composable
fun EmptyStateScreenComposableLightModeOnlinePreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isOnlineMode = true)
    }
}

@Preview(showBackground = true, name = "Light Mode - Favorites")
@Composable
fun EmptyStateScreenComposableLightModeFavoritesPreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isFavorites = true)
    }
}

@Preview(showBackground = true, name = "Dark Mode - Default", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyStateScreenComposableDarkModeDefaultPreview() {
    MaterialTheme {
        EmptyStateScreenComposable()
    }
}

@Preview(showBackground = true, name = "Dark Mode - Search", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyStateScreenComposableDarkModeSearchPreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isSearchResults = true)
    }
}

@Preview(showBackground = true, name = "Dark Mode - Online", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyStateScreenComposableDarkModeOnlinePreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isOnlineMode = true)
    }
}

@Preview(showBackground = true, name = "Dark Mode - Favorites", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyStateScreenComposableDarkModeFavoritesPreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isFavorites = true)
    }
}
