package com.ualachallenge.ui.screens

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import com.ualachallenge.ui.screens.citylist.CityListLandscapeScreenComposable
import com.ualachallenge.ui.screens.citylist.CityListScreenComposable

@Composable
fun OrientationAwareScreen(
    onCityClick: (Int) -> Unit,
    onMapClick: (Int) -> Unit = {},
    selectedCityId: Int? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    if (isLandscape) {
        // In landscape mode, don't navigate - just update internal state
        CityListLandscapeScreenComposable(
            onCityClick = { cityId ->
                // In landscape, we don't navigate - the map updates inline
                // The onCityClick callback is not used in landscape mode
            },
            onMapClick = { cityId ->
                // In landscape, we don't navigate - the map updates inline
                // The onMapClick callback is not used in landscape mode
            },
            selectedCityId = selectedCityId
        )
    } else {
        // In portrait mode, navigate to separate screens
        CityListScreenComposable(
            onCityClick = onCityClick,
            onMapClick = onMapClick,
            selectedCityId = selectedCityId
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun OrientationAwareScreenLightModePreview() {
    MaterialTheme {
        OrientationAwareScreen(
            onCityClick = {},
            onMapClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OrientationAwareScreenDarkModePreview() {
    MaterialTheme {
        OrientationAwareScreen(
            onCityClick = {},
            onMapClick = {}
        )
    }
} 
