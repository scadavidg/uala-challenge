package com.ualachallenge.ui.navigation

sealed class Screen(val route: String) {
    object CityList : Screen("city_list")
    object CityDetail : Screen("city_detail/{cityId}") {
        fun createRoute(cityId: Int) = "city_detail/$cityId"
    }
    object MapView : Screen("map_view/{cityId}") {
        fun createRoute(cityId: Int) = "map_view/$cityId"
    }
}
