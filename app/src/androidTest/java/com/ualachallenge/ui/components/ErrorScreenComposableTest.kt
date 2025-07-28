package com.ualachallenge.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ErrorScreenComposableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorScreen_displaysErrorTitle() {
        composeTestRule.setContent {
            ErrorScreenComposable(
                error = "Test error message",
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("Error").assertExists()
    }

    @Test
    fun errorScreen_displaysErrorMessage() {
        val errorMessage = "Network error occurred"

        composeTestRule.setContent {
            ErrorScreenComposable(
                error = errorMessage,
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertExists()
    }

    @Test
    fun errorScreen_showsRetryButton() {
        composeTestRule.setContent {
            ErrorScreenComposable(
                error = "Test error",
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("Retry").assertExists()
    }

    @Test
    fun errorScreen_callsOnRetry_whenRetryButtonClicked() {
        var retryClicked = false

        composeTestRule.setContent {
            ErrorScreenComposable(
                error = "Test error",
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        assert(retryClicked)
    }
}
