package com.ualachallenge.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ualachallenge.ui.screens.OrientationAwareScreen
import com.ualachallenge.ui.screens.mapview.MapViewScreenComposable

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = Screen.CityList.route
    ) {
        composable(route = Screen.CityList.route) {
            val selectedCityId = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("selectedCityId")
            var showExitDialog by remember { mutableStateOf(false) }
            
            // Handle back button press on main screen
            BackHandler(enabled = true) {
                // Show confirmation dialog when back is pressed on main screen
                showExitDialog = true
            }
            
            // Exit confirmation dialog
            if (showExitDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    icon = { 
                        androidx.compose.material.icons.Icons.Default.Warning
                    },
                    title = { 
                        androidx.compose.material3.Text(
                            text = "Exit Application",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        ) 
                    },
                    text = { 
                        androidx.compose.material3.Text(
                            text = "Are you sure you want to exit the application?",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                showExitDialog = false
                                // Close the app
                                (context as? androidx.activity.ComponentActivity)?.finish()
                            }
                        ) {
                            androidx.compose.material3.Text(
                                text = "Exit",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { showExitDialog = false }
                        ) {
                            androidx.compose.material3.Text("Cancel")
                        }
                    },
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    textContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
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
            
            // On MapView screen, let the normal back navigation work
            BackHandler(enabled = false) {
                // Disable custom back handling on MapView screen
                // Let the normal navigation work
            }
            
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
