package com.ualachallenge.ui.navigation

sealed class Screen(val route: String) {
    object CityList : Screen("city_list")
    object MapView : Screen("map_view/{cityId}") {
        fun createRoute(cityId: Int) = "map_view/$cityId"
    }
}
