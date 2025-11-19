package com.example.weatherapp.ui.theme.navigation

import PermissionRequester
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.weatherapp.R
import com.example.weatherapp.data.viewmodel.WeatherViewModel
import com.example.weatherapp.ui.theme.view.DayDetails
import com.example.weatherapp.ui.theme.view.WeatherScreen
import com.example.weatherapp.ui.theme.view.WelcomeScreen

@Composable
fun NavigationGraph(
    navController : NavHostController
) {
    val viewModel : WeatherViewModel = viewModel()
    val context = LocalContext.current

    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val hasInternet by viewModel.isNetworkAvailable.collectAsState()


    val requestPermission = PermissionRequester { granted ->
        viewModel.updatePermissionStatus(granted)
    }



    LaunchedEffect(Unit) {

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.updateFinePermission(fineGranted)
        viewModel.updateCoarsePermission(coarseGranted)
        viewModel.updatePermissionStatus(coarseGranted, fineGranted)
    }


    LaunchedEffect(hasLocationPermission, hasInternet) {
        if(hasLocationPermission) {
            if(viewModel.weatherResponse.value == null) viewModel.startLocationWorker(context)
        }
        else {
            requestPermission()
        }
    }

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
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val dayOfWeek = backStackEntry.arguments?.getString("dayOfWeek") ?: "UNKNOWN"
            val city = backStackEntry.arguments?.getString("city") ?: "UNKNOWN"
            val rawFile = backStackEntry.arguments?.getString("rawFile")?.toIntOrNull() ?: R.raw.weathersunny
            DayDetails(navController, date, viewModel, dayOfWeek, rawFile, city)
        }
    }
}
