package com.ualachallenge.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarComposable(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search cities...",
    leadingIcon: ImageVector = Icons.Default.Search,
    trailingIcon: ImageVector? = Icons.Default.Clear,
    onTrailingIconClick: (() -> Unit)? = null,
    isSearching: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = FocusRequester()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (isSearching) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), // Smaller size
                    strokeWidth = 1.5.dp // Thinner stroke
                )
            } else if (query.isNotEmpty() && trailingIcon != null) {
                IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        )
    )
}

@Preview(showBackground = true, name = "Light Mode - Empty")
@Composable
fun SearchBarComposableLightModeEmptyPreview() {
    MaterialTheme {
        SearchBarComposable(
            query = "",
            onQueryChange = {},
            onTrailingIconClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - With Text")
@Composable
fun SearchBarComposableLightModeWithTextPreview() {
    MaterialTheme {
        SearchBarComposable(
            query = "New York",
            onQueryChange = {},
            onTrailingIconClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode - Empty")
@Composable
fun SearchBarComposableDarkModeEmptyPreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        SearchBarComposable(
            query = "",
            onQueryChange = {},
            onTrailingIconClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode - With Text")
@Composable
fun SearchBarComposableDarkModeWithTextPreview() {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme()
    ) {
        SearchBarComposable(
            query = "New York",
            onQueryChange = {},
            onTrailingIconClick = {}
        )
    }
}
