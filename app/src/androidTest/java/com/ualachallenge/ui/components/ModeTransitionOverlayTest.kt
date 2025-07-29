package com.ualachallenge.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModeTransitionOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_online_mode_is_true_when_mode_transition_overlay_is_displayed_then_should_render_without_crashing() {
        // Given
        val isOnlineMode = true

        // When
        composeTestRule.setContent {
            ModeTransitionOverlay(
                isOnlineMode = isOnlineMode
            )
        }

        // Then
        // Component should render without crashing
        assertTrue(true)
    }

    @Test
    fun given_online_mode_is_false_when_mode_transition_overlay_is_displayed_then_should_render_without_crashing() {
        // Given
        val isOnlineMode = false

        // When
        composeTestRule.setContent {
            ModeTransitionOverlay(
                isOnlineMode = isOnlineMode
            )
        }

        // Then
        // Component should render without crashing
        assertTrue(true)
    }
}
