package com.ualachallenge.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.domain.models.City
import org.junit.Rule
import org.junit.Test

class CityItemComposableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCity =
        City(
            id = 1,
            name = "New York",
            country = "United States",
            lat = 40.7128,
            lon = -74.0060,
            isFavorite = true
        )

    @Test
    fun cityItem_displaysCityAndCountry() {
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithText("New York, United States").assertExists()
    }

    @Test
    fun cityItem_displaysCoordinates() {
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithText("📍 40.7128, -74.0060").assertExists()
    }

    @Test
    fun cityItem_showsFavoriteIcon_whenCityIsFavorite() {
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {}
            )
        }

        // Check that favorite icon is visible
        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertExists()
    }

    @Test
    fun cityItem_showsNotFavoriteIcon_whenCityIsNotFavorite() {
        val notFavoriteCity = testCity.copy(isFavorite = false)

        composeTestRule.setContent {
            CityItemComposable(
                city = notFavoriteCity,
                onClick = {}
            )
        }

        // Check that not favorite icon is visible
        composeTestRule.onNodeWithContentDescription("Add to favorites").assertExists()
    }

    @Test
    fun cityItem_showsMapButton() {
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Open map").assertExists()
    }

    @Test
    fun cityItem_showsInfoButton() {
        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("City information").assertExists()
    }

    @Test
    fun cityItem_callsOnClick_whenClicked() {
        var clicked = false

        composeTestRule.setContent {
            CityItemComposable(
                city = testCity,
                onClick = { clicked = true }
            )
        }

        // Click on the card area
        composeTestRule.onNodeWithText("New York, United States").performClick()

        assert(clicked)
    }
}
