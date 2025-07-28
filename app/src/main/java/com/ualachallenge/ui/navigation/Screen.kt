package com.ualachallenge.ui.navigation

sealed class Screen(val route: String) {
    object CityList : Screen("city_list")
}
