package com.example.weatherapp.ui.theme.navigation

sealed class Screen(val route : String) {

    object WelcomeScreen : Screen(route = "welcome_screen")

    object WeatherScreen : Screen(route = "weather_screen")
}