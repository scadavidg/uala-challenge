package com.ualachallenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ualachallenge.ui.screens.citylist.CityListScreenComposable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.CityList.route
    ) {
        composable(route = Screen.CityList.route) {
            CityListScreenComposable(
                onCityClick = { cityId ->
                    // For now, just show a simple message
                    // We'll implement city detail later
                }
            )
        }
    }
}
