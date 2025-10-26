package com.example.weatherapp.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.weatherapp.ui.theme.view.WeatherScreen
import com.example.weatherapp.ui.theme.view.WelcomeScreen

@Composable
fun NavigationGraph(
    navController : NavHostController
) {
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
            WeatherScreen(navController)
        }

    }
}