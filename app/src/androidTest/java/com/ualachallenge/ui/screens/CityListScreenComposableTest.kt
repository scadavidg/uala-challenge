package com.ualachallenge.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ualachallenge.ui.components.EmptyStateScreenComposable
import com.ualachallenge.ui.components.ErrorScreenComposable
import org.junit.Rule
import org.junit.Test

class CityListScreenComposableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateScreen_displaysCorrectly() {
        // When
        composeTestRule.setContent {
            EmptyStateScreenComposable()
        }

        // Then
        composeTestRule.onNodeWithText("No cities available").assertIsDisplayed()
    }

    @Test
    fun errorScreen_displaysCorrectly() {
        // When
        composeTestRule.setContent {
            ErrorScreenComposable(
                error = "Test error message",
                onRetry = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test error message").assertIsDisplayed()
    }
}
