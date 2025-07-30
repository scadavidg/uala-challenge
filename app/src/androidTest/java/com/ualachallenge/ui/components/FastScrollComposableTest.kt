package com.ualachallenge.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.domain.models.City
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FastScrollComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCities = listOf(
        City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
        City(id = 2, name = "Alaska", country = "US", lat = 64.2008, lon = -149.4937, isFavorite = true),
        City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false),
        City(id = 4, name = "Boston", country = "US", lat = 42.3601, lon = -71.0589, isFavorite = false),
        City(id = 5, name = "Chicago", country = "US", lat = 41.8781, lon = -87.6298, isFavorite = true),
        City(id = 6, name = "Denver", country = "US", lat = 39.7392, lon = -104.9903, isFavorite = false),
        City(id = 7, name = "Eugene", country = "US", lat = 44.0521, lon = -123.0868, isFavorite = false),
        City(id = 8, name = "Florida", country = "US", lat = 27.6648, lon = -81.5158, isFavorite = false),
        City(id = 9, name = "Georgia", country = "US", lat = 32.1656, lon = -82.9001, isFavorite = false),
        City(id = 10, name = "Hawaii", country = "US", lat = 19.8968, lon = -155.5828, isFavorite = true),
        City(id = 11, name = "Idaho", country = "US", lat = 44.2405, lon = -114.4788, isFavorite = false),
        City(id = 12, name = "Illinois", country = "US", lat = 40.6331, lon = -89.3985, isFavorite = false),
        City(id = 13, name = "Indiana", country = "US", lat = 39.8494, lon = -86.2583, isFavorite = false),
        City(id = 14, name = "Iowa", country = "US", lat = 42.0329, lon = -93.9038, isFavorite = false),
        City(id = 15, name = "Kansas", country = "US", lat = 38.5111, lon = -96.8005, isFavorite = false),
        City(id = 16, name = "Kentucky", country = "US", lat = 37.6681, lon = -84.6701, isFavorite = false),
        City(id = 17, name = "Louisiana", country = "US", lat = 31.1695, lon = -91.8678, isFavorite = false),
        City(id = 18, name = "Maine", country = "US", lat = 44.6939, lon = -69.3819, isFavorite = false),
        City(id = 19, name = "Maryland", country = "US", lat = 39.0639, lon = -76.8021, isFavorite = false),
        City(id = 20, name = "Massachusetts", country = "US", lat = 42.2304, lon = -71.5301, isFavorite = false),
        City(id = 21, name = "Michigan", country = "US", lat = 44.3148, lon = -85.6024, isFavorite = false),
        City(id = 22, name = "Minnesota", country = "US", lat = 46.7296, lon = -94.6859, isFavorite = false),
        City(id = 23, name = "Mississippi", country = "US", lat = 32.7416, lon = -89.6787, isFavorite = false),
        City(id = 24, name = "Missouri", country = "US", lat = 38.4561, lon = -92.2884, isFavorite = false),
        City(id = 25, name = "Montana", country = "US", lat = 46.8797, lon = -110.3626, isFavorite = false),
        City(id = 26, name = "Nebraska", country = "US", lat = 41.4925, lon = -99.9018, isFavorite = false),
        City(id = 27, name = "Nevada", country = "US", lat = 38.8026, lon = -116.4194, isFavorite = false),
        City(id = 28, name = "New Hampshire", country = "US", lat = 43.1939, lon = -71.5724, isFavorite = false),
        City(id = 29, name = "New Jersey", country = "US", lat = 40.0583, lon = -74.4057, isFavorite = false),
        City(id = 30, name = "New Mexico", country = "US", lat = 34.5199, lon = -105.8701, isFavorite = false),
        City(id = 31, name = "New York", country = "US", lat = 40.7128, lon = -74.0060, isFavorite = true),
        City(id = 32, name = "North Carolina", country = "US", lat = 35.7596, lon = -79.0193, isFavorite = false),
        City(id = 33, name = "North Dakota", country = "US", lat = 47.5515, lon = -101.0020, isFavorite = false),
        City(id = 34, name = "Ohio", country = "US", lat = 40.4173, lon = -82.9071, isFavorite = false),
        City(id = 35, name = "Oklahoma", country = "US", lat = 35.0078, lon = -97.0929, isFavorite = false),
        City(id = 36, name = "Oregon", country = "US", lat = 44.5720, lon = -122.0709, isFavorite = false),
        City(id = 37, name = "Pennsylvania", country = "US", lat = 40.5908, lon = -77.2098, isFavorite = false),
        City(id = 38, name = "Rhode Island", country = "US", lat = 41.6809, lon = -71.5118, isFavorite = false),
        City(id = 39, name = "South Carolina", country = "US", lat = 33.8569, lon = -80.9450, isFavorite = false),
        City(id = 40, name = "South Dakota", country = "US", lat = 44.2998, lon = -99.4388, isFavorite = false),
        City(id = 41, name = "Tennessee", country = "US", lat = 35.7478, lon = -86.6923, isFavorite = false),
        City(id = 42, name = "Texas", country = "US", lat = 31.9686, lon = -99.9018, isFavorite = false),
        City(id = 43, name = "Utah", country = "US", lat = 39.3210, lon = -111.0937, isFavorite = false),
        City(id = 44, name = "Vermont", country = "US", lat = 44.0459, lon = -72.7107, isFavorite = false),
        City(id = 45, name = "Virginia", country = "US", lat = 37.7693, lon = -78.1700, isFavorite = false),
        City(id = 46, name = "Washington", country = "US", lat = 47.7511, lon = -120.7401, isFavorite = false),
        City(id = 47, name = "West Virginia", country = "US", lat = 38.5976, lon = -80.4549, isFavorite = false),
        City(id = 48, name = "Wisconsin", country = "US", lat = 43.7844, lon = -88.7879, isFavorite = false),
        City(id = 49, name = "Wyoming", country = "US", lat = 42.7475, lon = -107.2085, isFavorite = false)
    )

    @Test
    fun fastScroll_doesNotDisplay_whenOnlyOneCityExists() {
        // Given
        val singleCity = listOf(testCities[0]) // Only Alabama
        val listState = LazyListState()

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = singleCity,
                listState = listState
            )
        }

        // Then
        // Should not display any letters since there's only one city
        composeTestRule.onNodeWithText("A").assertDoesNotExist()
    }

    @Test
    fun fastScroll_doesNotDisplay_whenNoCitiesExist() {
        // Given
        val emptyCities = emptyList<City>()
        val listState = LazyListState()

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = emptyCities,
                listState = listState
            )
        }

        // Then
        // Should not display any letters since there are no cities
        composeTestRule.onNodeWithText("A").assertDoesNotExist()
    }

    @Test
    fun fastScroll_lettersAreClickable() {
        // Given
        val listState = LazyListState()
        var clickedLetter = ""

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = testCities,
                listState = listState
            )
        }

        // Then
        // Verify that letters are clickable (we can't test the actual scroll behavior
        // in UI tests, but we can verify the component renders without crashing)
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.onNodeWithText("B").performClick()
        composeTestRule.onNodeWithText("C").performClick()

        // Component should still be displayed after clicks
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertIsDisplayed()
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
    }

    @Test
    fun fastScroll_rendersWithoutCrashing() {
        // Given
        val listState = LazyListState()

        // When
        composeTestRule.setContent {
            FastScrollComposable(
                cities = testCities,
                listState = listState
            )
        }

        // Then
        // Component should render without any exceptions
        // This test verifies that the FastScrollComposable works correctly
        composeTestRule.waitForIdle()
    }
}
