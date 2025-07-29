package com.ualachallenge.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
    isSearchResults: Boolean = false
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
                else -> Icons.Default.LocationOff
            },
            contentDescription = when {
                isSearchResults -> "Search icon"
                isOnlineMode -> "No internet connection"
                else -> "No cities available"
            },
            modifier = Modifier.size(64.dp),
            tint = when {
                isSearchResults -> MaterialTheme.colorScheme.primary
                isOnlineMode -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                isSearchResults -> "No results found"
                isOnlineMode -> "Unable to load cities"
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
                else -> "Check your internet connection"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateScreenComposablePreview() {
    MaterialTheme {
        EmptyStateScreenComposable()
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateScreenComposableSearchPreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isSearchResults = true)
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateScreenComposableOnlinePreview() {
    MaterialTheme {
        EmptyStateScreenComposable(isOnlineMode = true)
    }
}
