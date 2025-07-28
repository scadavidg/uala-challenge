package com.ualachallenge.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchBarComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_shouldDisplayPlaceholder() {
        // Given
        var searchQuery by mutableStateOf("")
        val onQueryChange: (String) -> Unit = { query -> searchQuery = query }

        // When
        composeTestRule.setContent {
            SearchBarComposable(
                query = searchQuery,
                onQueryChange = onQueryChange
            )
        }

        // Then
        composeTestRule.onNodeWithText("Search cities...").assertIsDisplayed()
    }

    @Test
    fun searchBar_shouldShowClearButtonWhenTextIsNotEmpty() {
        // Given
        var searchQuery by mutableStateOf("Test")
        val onQueryChange: (String) -> Unit = { query -> searchQuery = query }
        val onClearClick: () -> Unit = { searchQuery = "" }

        // When
        composeTestRule.setContent {
            SearchBarComposable(
                query = searchQuery,
                onQueryChange = onQueryChange,
                onTrailingIconClick = onClearClick
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    @Test
    fun searchBar_shouldShowSearchIcon() {
        // Given
        var searchQuery by mutableStateOf("")
        val onQueryChange: (String) -> Unit = { query -> searchQuery = query }

        // When
        composeTestRule.setContent {
            SearchBarComposable(
                query = searchQuery,
                onQueryChange = onQueryChange
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }
}
