package com.ualachallenge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CacheMigrationIndicator(
    progress: Float,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "migration_progress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cache icon or indicator
        CircularProgressIndicator(
            progress = { animatedProgress / 100f },
            modifier = Modifier.size(24.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )

        // Progress text
        Text(
            text = "Cache Migration",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Progress percentage
        Text(
            text = "${animatedProgress.toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        // Linear progress indicator
        LinearProgressIndicator(
            progress = { animatedProgress / 100f },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Preview(showBackground = true, name = "Visible - Light Mode")
@Composable
fun CacheMigrationIndicatorVisibleLightModePreview() {
    MaterialTheme {
        CacheMigrationIndicator(
            progress = 75f,
            isVisible = true
        )
    }
}

@Preview(showBackground = true, name = "Hidden - Light Mode")
@Composable
fun CacheMigrationIndicatorHiddenLightModePreview() {
    MaterialTheme {
        CacheMigrationIndicator(
            progress = 50f,
            isVisible = false
        )
    }
}

@Preview(showBackground = true, name = "Visible - Dark Mode")
@Composable
fun CacheMigrationIndicatorVisibleDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        CacheMigrationIndicator(
            progress = 75f,
            isVisible = true
        )
    }
}

@Preview(showBackground = true, name = "Hidden - Dark Mode")
@Composable
fun CacheMigrationIndicatorHiddenDarkModePreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        CacheMigrationIndicator(
            progress = 50f,
            isVisible = false
        )
    }
}
