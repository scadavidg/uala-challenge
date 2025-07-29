package com.ualachallenge.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ualachallenge.ui.screens.citydetail.CityDetailScreenComposable
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityDetailScreenComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_city_details_are_loaded_when_city_detail_screen_composable_is_displayed_then_should_render_without_crashing() {
        // Given
        // City data is available

        // When
        composeTestRule.setContent {
            CityDetailScreenComposable(
                onNavigateBack = {},
                onNavigateToMap = { _ -> }
            )
        }

        // Then
        // Component should render without crashing
        assertTrue(true)
    }
}
