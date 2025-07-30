package com.ualachallenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreenComposable() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(5) { // Reduced from 10 to 5 skeleton items
            SkeletonCityItemComposable()
        }
    }
}

@Composable
fun SkeletonCityItemComposable() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp) // Slightly smaller
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), // More subtle
                        shape = MaterialTheme.shapes.small
                    )
            )
            Spacer(modifier = Modifier.height(6.dp)) // Reduced spacing
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Smaller width
                    .height(14.dp) // Smaller height
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // More subtle
                        shape = MaterialTheme.shapes.small
                    )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f) // Smaller width
                    .height(12.dp) // Smaller height
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // More subtle
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoadingScreenComposableLightModePreview() {
    MaterialTheme {
        LoadingScreenComposable()
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun LoadingScreenComposableDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        LoadingScreenComposable()
    }
}

@Preview(showBackground = true, name = "Skeleton Light Mode")
@Composable
fun SkeletonCityItemComposableLightModePreview() {
    MaterialTheme {
        SkeletonCityItemComposable()
    }
}

@Preview(showBackground = true, name = "Skeleton Dark Mode")
@Composable
fun SkeletonCityItemComposableDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        SkeletonCityItemComposable()
    }
}
