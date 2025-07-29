package com.ualachallenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OnlineModeIndicatorComposable(isOnlineMode: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = if (isOnlineMode) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isOnlineMode) Icons.Default.Wifi else Icons.Default.WifiOff,
            contentDescription = if (isOnlineMode) "Online mode" else "Offline mode",
            modifier = Modifier.size(16.dp),
            tint = if (isOnlineMode) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Text(
            text = if (isOnlineMode) "Online" else "Offline",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (isOnlineMode) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnlineModeIndicatorOnlinePreview() {
    MaterialTheme {
        OnlineModeIndicatorComposable(isOnlineMode = true)
    }
}

@Preview(showBackground = true)
@Composable
fun OnlineModeIndicatorOfflinePreview() {
    MaterialTheme {
        OnlineModeIndicatorComposable(isOnlineMode = false)
    }
}
