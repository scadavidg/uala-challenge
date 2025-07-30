package com.ualachallenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ualachallenge.ui.screens.OrientationAwareScreen
import com.ualachallenge.ui.screens.mapview.MapViewScreenComposable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.CityList.route
    ) {
        composable(route = Screen.CityList.route) {
            val selectedCityId = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("selectedCityId")
            OrientationAwareScreen(
                onCityClick = { cityId ->
                    navController.navigate(Screen.MapView.createRoute(cityId))
                },
                onMapClick = { cityId ->
                    navController.navigate(Screen.MapView.createRoute(cityId))
                },
                selectedCityId = selectedCityId
            )
        }

        composable(
            route = Screen.MapView.route,
            arguments = listOf(
                navArgument("cityId") { type = NavType.IntType }
            )
        ) {
            val cityId = it.arguments?.getInt("cityId") ?: 0
            MapViewScreenComposable(
                onNavigateBack = { selectedCityId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedCityId", selectedCityId)
                    navController.popBackStack()
                },
                cityId = cityId
            )
        }
    }
}
