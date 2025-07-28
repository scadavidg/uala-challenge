package com.ualachallenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ualachallenge.ui.screens.citylist.CityListScreenComposable
import com.ualachallenge.ui.screens.mapview.MapViewScreenComposable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.CityList.route
    ) {
        composable(route = Screen.CityList.route) {
            CityListScreenComposable(
                onCityClick = { cityId ->
                    navController.navigate(Screen.MapView.createRoute(cityId))
                },
                onMapClick = { cityId ->
                    navController.navigate(Screen.MapView.createRoute(cityId))
                }
            )
        }

        composable(
            route = Screen.MapView.route,
            arguments = listOf(
                navArgument("cityId") { type = NavType.IntType }
            )
        ) {
            MapViewScreenComposable(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
