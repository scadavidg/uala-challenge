package com.ualachallenge.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnlineModeIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onlineModeIndicator_shouldDisplayOnlineText_whenIsOnlineModeIsTrue() {
        // Given
        val isOnlineMode = true

        // When
        composeTestRule.setContent {
            OnlineModeIndicatorComposable(isOnlineMode = isOnlineMode)
        }

        // Then
        composeTestRule.onNodeWithText("Online").assertIsDisplayed()
    }

    @Test
    fun onlineModeIndicator_shouldDisplayOfflineText_whenIsOnlineModeIsFalse() {
        // Given
        val isOnlineMode = false

        // When
        composeTestRule.setContent {
            OnlineModeIndicatorComposable(isOnlineMode = isOnlineMode)
        }

        // Then
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
    }
}
