package com.ualachallenge.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class EmptyStateScreenComposableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateScreen_displaysTitle() {
        // Given
        // EmptyStateScreenComposable component

        // When
        composeTestRule.setContent {
            EmptyStateScreenComposable()
        }

        // Then
        composeTestRule.onNodeWithText("No cities available").assertExists()
    }

    @Test
    fun emptyStateScreen_displaysSubtitle() {
        // Given
        // EmptyStateScreenComposable component

        // When
        composeTestRule.setContent {
            EmptyStateScreenComposable()
        }

        // Then
        composeTestRule.onNodeWithText("Check your internet connection").assertExists()
    }
}
