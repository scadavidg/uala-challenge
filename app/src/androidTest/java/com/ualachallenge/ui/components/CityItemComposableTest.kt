package com.ualachallenge.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.domain.models.City
import org.junit.Rule
import org.junit.Test

class CityItemComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCity = City(
        id = 1,
        name = "New York",
        country = "United States",
        lat = 40.7128,
        lon = -74.0060,
        isFavorite = false
    )

    @Test
    fun cityItem_displaysCorrectly() {
        // Given
        var favoriteToggleCount = 0

        // When
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {},
                onFavoriteToggle = { favoriteToggleCount++ }
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Add to favorites").assertIsDisplayed()
    }

    @Test
    fun favoriteButton_toggleWorks() {
        // Given
        var favoriteToggleCount = 0

        // When
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {},
                onFavoriteToggle = { favoriteToggleCount++ }
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()
        assert(favoriteToggleCount == 1)
    }

    @Test
    fun favoriteButton_showsCorrectIcon_whenFavorite() {
        // Given
        val favoriteCity = testCity.copy(isFavorite = true)

        // When
        composeTestRule.setContent {
            CityItemComposable(
                city = favoriteCity,
                onClick = {},
                onFavoriteToggle = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }
}
