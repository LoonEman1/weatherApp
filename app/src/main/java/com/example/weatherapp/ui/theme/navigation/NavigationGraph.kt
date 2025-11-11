package com.example.weatherapp.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.weatherapp.data.viewmodel.WeatherViewModel
import com.example.weatherapp.ui.theme.view.DayDetails
import com.example.weatherapp.ui.theme.view.WeatherScreen
import com.example.weatherapp.ui.theme.view.WelcomeScreen

@Composable
fun NavigationGraph(
    navController : NavHostController
) {
    val viewModel : WeatherViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Screen.WelcomeScreen.route
    ) {
        composable(
            route = Screen.WelcomeScreen.route
        )
        {
            WelcomeScreen(navController)
        }

        composable(
            route = Screen.WeatherScreen.route
        )
        {
            WeatherScreen(navController, viewModel)
        }

        composable(
            route = Screen.DayDetailsScreen.route
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            DayDetails(navController, date, viewModel)
        }
    }
}