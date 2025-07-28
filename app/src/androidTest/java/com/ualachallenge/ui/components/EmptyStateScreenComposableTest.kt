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
        composeTestRule.setContent {
            EmptyStateScreenComposable()
        }

        composeTestRule.onNodeWithText("No cities available").assertExists()
    }

    @Test
    fun emptyStateScreen_displaysSubtitle() {
        composeTestRule.setContent {
            EmptyStateScreenComposable()
        }

        composeTestRule.onNodeWithText("Check your internet connection").assertExists()
    }
}
