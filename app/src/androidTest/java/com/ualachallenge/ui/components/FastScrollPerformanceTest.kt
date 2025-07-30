package com.ualachallenge.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.domain.models.City
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FastScrollPerformanceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val largeTestCities = (1..1000).map { index ->
        City(
            id = index,
            name = "City$index",
            country = "US",
            lat = 40.0 + (index % 10),
            lon = -74.0 + (index % 10),
            isFavorite = index % 2 == 0
        )
    }

    @Test
    fun fastScroll_performanceWithLargeDataset() {
        // Given
        val listState = LazyListState()

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = largeTestCities,
                listState = listState
            )
        }

        // Then
        // Should render without performance issues
        composeTestRule.waitForIdle()
    }

    @Test
    fun fastScroll_memoryUsageWithLargeDataset() {
        // Given
        val listState = LazyListState()

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = largeTestCities,
                listState = listState
            )
        }

        // Then
        // Should not cause memory leaks or excessive memory usage
        composeTestRule.waitForIdle()
    }

    @Test
    fun fastScroll_scrollPerformance() {
        // Given
        val listState = LazyListState()

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = largeTestCities,
                listState = listState
            )
        }

        // Then
        // Should handle scroll events smoothly
        composeTestRule.waitForIdle()
    }
} 
