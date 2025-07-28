package com.ualachallenge.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import com.domain.models.City
import org.junit.Rule
import org.junit.Test

class CityListComposablePerformanceTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun generateLargeCityList(size: Int): List<City> = (1..size).map { index ->
        City(
            id = index,
            name = "City $index",
            country = "Country $index",
            lat = 40.0 + (index * 0.1),
            lon = -74.0 + (index * 0.1),
            isFavorite = index % 2 == 0
        )
    }

    @Test
    fun cityList_rendersLargeList_under2Seconds() {
        val largeCityList = generateLargeCityList(1000)
        var renderTime = 0L

        composeTestRule.setContent {
            val startTime = System.currentTimeMillis()
            CityListComposable(
                cities = largeCityList,
                onCityClick = {},
                onFavoriteToggle = {}
            )
            renderTime = System.currentTimeMillis() - startTime
        }

        // Verify performance: should render in under 2 seconds
        assert(renderTime < 2000) { "Large list took ${renderTime}ms to render, expected under 2000ms" }
    }

    @Test
    fun cityList_rendersMediumList_under1Second() {
        val mediumCityList = generateLargeCityList(500)
        var renderTime = 0L

        composeTestRule.setContent {
            val startTime = System.currentTimeMillis()
            CityListComposable(
                cities = mediumCityList,
                onCityClick = {},
                onFavoriteToggle = {}
            )
            renderTime = System.currentTimeMillis() - startTime
        }

        // Verify performance: should render in under 1 second
        assert(renderTime < 1000) { "Medium list took ${renderTime}ms to render, expected under 1000ms" }
    }

    @Test
    fun cityList_memoryUsage_largeList() {
        val largeCityList = generateLargeCityList(2000)

        // Get initial memory usage
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        composeTestRule.setContent {
            CityListComposable(
                cities = largeCityList,
                onCityClick = {},
                onFavoriteToggle = {}
            )
        }

        // Force garbage collection and measure memory
        System.gc()
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory

        // Verify memory usage: should use less than 50MB
        val memoryUsedMB = memoryUsed / (1024 * 1024)
        assert(memoryUsedMB < 50) { "Memory usage: ${memoryUsedMB}MB, expected under 50MB" }
    }
}
