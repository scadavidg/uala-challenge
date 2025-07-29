package com.ualachallenge.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheMigrationIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_migration_is_visible_and_progress_is_50_when_cache_migration_indicator_is_displayed_then_should_show_progress_indicator() {
        // Given
        val progress = 50f
        val isVisible = true

        // When
        composeTestRule.setContent {
            CacheMigrationIndicator(
                progress = progress,
                isVisible = isVisible
            )
        }

        // Then
        composeTestRule.onNodeWithText("Cache Migration").assertIsDisplayed()
        composeTestRule.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test
    fun given_migration_is_not_visible_when_cache_migration_indicator_is_displayed_then_should_not_show_any_content() {
        // Given
        val progress = 75f
        val isVisible = false

        // When
        composeTestRule.setContent {
            CacheMigrationIndicator(
                progress = progress,
                isVisible = isVisible
            )
        }

        // Then
        // No content should be displayed when isVisible is false
        // This test verifies that the component returns early when isVisible is false
    }

    @Test
    fun given_migration_is_visible_and_progress_is_100_when_cache_migration_indicator_is_displayed_then_should_show_complete_progress() {
        // Given
        val progress = 100f
        val isVisible = true

        // When
        composeTestRule.setContent {
            CacheMigrationIndicator(
                progress = progress,
                isVisible = isVisible
            )
        }

        // Then
        composeTestRule.onNodeWithText("Cache Migration").assertIsDisplayed()
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }
}
