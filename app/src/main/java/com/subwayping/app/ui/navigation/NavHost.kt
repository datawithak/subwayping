package com.subwayping.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subwayping.app.ui.screens.HomeScreen
import com.subwayping.app.ui.screens.LandingScreen
import com.subwayping.app.ui.screens.RoutePickerScreen
import com.subwayping.app.ui.screens.SettingsScreen

object Routes {
    const val LANDING = "landing"
    const val HOME = "home"
    const val ROUTE_PICKER = "route_picker/{mode}"  // mode = "favourite" or "customize"
    const val SETTINGS = "settings"

    fun routePicker(mode: String) = "route_picker/$mode"
}

@Composable
fun SubwayPingNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LANDING
    ) {
        composable(Routes.LANDING) {
            LandingScreen(
                onEnterFavourite = {
                    navController.navigate(Routes.routePicker("favourite"))
                },
                onCustomizeRoute = {
                    navController.navigate(Routes.routePicker("customize"))
                },
                onGoToPing = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LANDING) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToRoutePicker = {
                    navController.navigate(Routes.routePicker("customize"))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateBack = {
                    navController.popBackStack(Routes.LANDING, inclusive = false)
                }
            )
        }

        composable(
            route = Routes.ROUTE_PICKER,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "customize"
            RoutePickerScreen(
                isFavouriteMode = mode == "favourite",
                onRouteSelected = {
                    // After selecting, go to the PING home screen
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LANDING) { inclusive = false }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
