package com.ualachallenge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeTransitionOverlay(
    isOnlineMode: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode icon
                Icon(
                    imageVector = if (isOnlineMode) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = if (isOnlineMode) "Online Mode" else "Offline Mode",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode text
                Text(
                    text = if (isOnlineMode) "Cambiando a modo online..." else "Cambiando a modo offline...",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = if (isOnlineMode) {
                        "Cargando datos desde la API..."
                    } else {
                        "Cargando datos locales..."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Online - Light Mode")
@Composable
fun ModeTransitionOverlayOnlineLightModePreview() {
    MaterialTheme {
        ModeTransitionOverlay(isOnlineMode = true)
    }
}

@Preview(showBackground = true, name = "Offline - Light Mode")
@Composable
fun ModeTransitionOverlayOfflineLightModePreview() {
    MaterialTheme {
        ModeTransitionOverlay(isOnlineMode = false)
    }
}

@Preview(showBackground = true, name = "Online - Dark Mode")
@Composable
fun ModeTransitionOverlayOnlineDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        ModeTransitionOverlay(isOnlineMode = true)
    }
}

@Preview(showBackground = true, name = "Offline - Dark Mode")
@Composable
fun ModeTransitionOverlayOfflineDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        ModeTransitionOverlay(isOnlineMode = false)
    }
}
