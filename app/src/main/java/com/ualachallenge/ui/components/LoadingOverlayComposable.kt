package com.ualachallenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoadingOverlayComposable(modifier: Modifier = Modifier, backgroundColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer { alpha = 0.99f }, // Fix for some Compose z-order issues
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoadingOverlayComposableLightModePreview() {
    MaterialTheme {
        LoadingOverlayComposable()
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun LoadingOverlayComposableDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        LoadingOverlayComposable()
    }
}
