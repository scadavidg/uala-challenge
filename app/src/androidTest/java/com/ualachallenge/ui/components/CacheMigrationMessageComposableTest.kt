package com.ualachallenge.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheMigrationMessageComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_cache_migration_message_composable_is_displayed_when_component_is_rendered_then_should_show_cache_migration_message() {
        // Given
        // Component is ready to be displayed

        // When
        composeTestRule.setContent {
            CacheMigrationMessageComposable()
        }

        // Then
        composeTestRule.onNodeWithText("Cache en progreso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Los datos se están cargando en segundo plano. Puedes usar el modo online mientras tanto.").assertIsDisplayed()
        composeTestRule.onNodeWithText("El modo online está disponible para acceder a datos remotos").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cache Migration").assertIsDisplayed()
    }
}
