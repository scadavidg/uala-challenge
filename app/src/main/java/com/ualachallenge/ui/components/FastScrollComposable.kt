package com.ualachallenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domain.models.City
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

@Composable
fun FastScrollComposable(
    cities: List<City>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val groupedCities = cities.groupBy { it.name.first().uppercase() }
        .toSortedMap()

    val availableLetters = groupedCities.keys.toList()

    if (availableLetters.size > 1) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Fast scroll indicator centered on the right side
            FastScrollIndicator(
                availableLetters = availableLetters,
                groupedCities = groupedCities,
                listState = listState,
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun FastScrollIndicator(
    availableLetters: List<String>,
    groupedCities: Map<String, List<City>>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    var isScrolling by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Get orientation outside of derivedStateOf
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val maxItems = if (isLandscape) 5 else 12 // 5 for landscape, 12 for portrait

    // Track current visible letter based on scroll position
    val currentVisibleLetter by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo

            // Find which letter section is most visible
            var currentIndex = 0
            var mostVisibleLetter = availableLetters.firstOrNull() ?: ""

            groupedCities.forEach { (letter, citiesInGroup) ->
                val letterHeaderIndex = currentIndex
                val letterEndIndex = currentIndex + citiesInGroup.size

                // Check if this letter section is visible
                if (visibleItems.any { it.index >= letterHeaderIndex && it.index <= letterEndIndex }) {
                    mostVisibleLetter = letter
                }

                currentIndex += citiesInGroup.size + 1 // +1 for letter header
            }

            mostVisibleLetter
        }
    }

    // Calculate which letters to show based on current position and orientation
    val visibleLetters by remember {
        derivedStateOf {
            val currentLetterIndex = availableLetters.indexOf(currentVisibleLetter)

            if (currentLetterIndex == -1) {
                // If no current letter found, show first maxItems
                availableLetters.take(maxItems)
            } else {
                // Calculate the range to show letters centered around current
                val startIndex = max(0, currentLetterIndex - (maxItems / 2)) // Show half before current
                val endIndex = min(availableLetters.size, startIndex + maxItems) // Show up to maxItems letters

                // Adjust if we're near the end
                val adjustedStartIndex = max(0, endIndex - maxItems)
                availableLetters.subList(adjustedStartIndex, endIndex)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        visibleLetters.forEach { letter ->
            FastScrollLetter(
                letter = letter,
                onClick = {
                    coroutineScope.launch {
                        scrollToLetter(letter, groupedCities, listState)
                    }
                },
                isActive = letter == currentVisibleLetter,
                modifier = Modifier
                    .size(36.dp)
                    .padding(vertical = 2.dp)
            )
        }
    }

    // Auto-hide indicator after scrolling
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            kotlinx.coroutines.delay(2000)
            isScrolling = false
        }
    }
}

@Composable
private fun FastScrollLetter(
    letter: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                },
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primary
            },
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
    }
}

private suspend fun scrollToLetter(
    targetLetter: String,
    groupedCities: Map<String, List<City>>,
    listState: LazyListState
) {
    var currentIndex = 0
    var targetIndex = -1

    groupedCities.forEach { (letter, citiesInGroup) ->
        if (targetIndex == -1) {
            if (letter == targetLetter) {
                targetIndex = currentIndex
            } else {
                currentIndex += citiesInGroup.size + 1 // +1 for the letter header
            }
        }
    }

    if (targetIndex >= 0) {
        listState.animateScrollToItem(targetIndex)
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun FastScrollComposableLightModePreview() {
    val mockCities = listOf(
        City(1, "Amsterdam", "Netherlands", 52.3676, 4.9041, false),
        City(2, "Berlin", "Germany", 52.5200, 13.4050, true),
        City(3, "Copenhagen", "Denmark", 55.6761, 12.5683, false),
        City(4, "Dublin", "Ireland", 53.3498, -6.2603, true),
        City(5, "Edinburgh", "Scotland", 55.9533, -3.1883, false),
        City(6, "Frankfurt", "Germany", 50.1109, 8.6821, true),
        City(7, "Geneva", "Switzerland", 46.2044, 6.1432, false),
        City(8, "Hamburg", "Germany", 53.5511, 9.9937, true),
        City(9, "Istanbul", "Turkey", 41.0082, 28.9784, false),
        City(10, "Jerusalem", "Israel", 31.7683, 35.2137, true)
    )

    MaterialTheme {
        FastScrollComposable(
            cities = mockCities,
            listState = androidx.compose.foundation.lazy.rememberLazyListState()
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun FastScrollComposableDarkModePreview() {
    val mockCities = listOf(
        City(1, "Amsterdam", "Netherlands", 52.3676, 4.9041, false),
        City(2, "Berlin", "Germany", 52.5200, 13.4050, true),
        City(3, "Copenhagen", "Denmark", 55.6761, 12.5683, false),
        City(4, "Dublin", "Ireland", 53.3498, -6.2603, true),
        City(5, "Edinburgh", "Scotland", 55.9533, -3.1883, false),
        City(6, "Frankfurt", "Germany", 50.1109, 8.6821, true),
        City(7, "Geneva", "Switzerland", 46.2044, 6.1432, false),
        City(8, "Hamburg", "Germany", 53.5511, 9.9937, true),
        City(9, "Istanbul", "Turkey", 41.0082, 28.9784, false),
        City(10, "Jerusalem", "Israel", 31.7683, 35.2137, true)
    )

    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        FastScrollComposable(
            cities = mockCities,
            listState = androidx.compose.foundation.lazy.rememberLazyListState()
        )
    }
}
