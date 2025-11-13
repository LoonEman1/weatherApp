package com.example.weatherapp.ui.theme.view

import CurrentWeatherCard
import PermissionRequester
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.R
import com.example.weatherapp.data.model.DayForecast
import com.example.weatherapp.data.viewmodel.WeatherUIState
import com.example.weatherapp.data.viewmodel.WeatherViewModel
import com.example.weatherapp.ui.theme.view.common.WeatherText
import com.example.weatherapp.ui.theme.view.common.formatTemperature

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeatherScreen(navController: NavHostController, viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    viewModel.setLocale(locale)


    val hasFinePermission by viewModel.hasFinePermission.collectAsState()
    val hasCoarsePermissions by viewModel.hasCoarsePermission.collectAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val city by viewModel.geoCity.collectAsState()
    val weatherUIData by viewModel.weatherUIData.collectAsState()

    val backgroundRes by viewModel.backgroundRes.collectAsState()

    val context = LocalContext.current


    LaunchedEffect(Unit) {
        if (!hasFinePermission || !hasCoarsePermissions) {
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
            viewModel.updatePermissionStatus(fineGranted, coarseGranted)
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(backgroundRes))

    val requestPermissions = PermissionRequester { granted ->
        viewModel.updatePermissionStatus(granted)
    }

    LaunchedEffect(hasLocationPermission) {
        if(hasLocationPermission) {
            if(viewModel.weatherResponse.value == null) viewModel.startLocationWorker(context)
        }
        else {
            requestPermissions()
        }
    }


    Scaffold(
    ) { innerPadding ->
        val padding = innerPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.FillBounds,
                speed = 0.6f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.Start
            ) {
                when (uiState) {
                    is WeatherUIState.Loading -> {
                       WeatherText("Загрузка данных...")
                    }

                    is WeatherUIState.Error -> {
                        val message = (uiState as WeatherUIState.Error).message
                        WeatherText("Ошибка: $message")
                    }

                    is WeatherUIState.Success -> {
                        CurrentWeatherCard(weatherUIData, city ?: "unknown")


                        Spacer(modifier = Modifier.height(16.dp))

                        DailyForecastList(weatherUIData.dailyForecasts, locale) { day ->
                            navController.navigate("day_details/${day.date}/${day.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                            }}/${day.weatherDescription?.lottieFile}/$city")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastList(dayForecasts: List<DayForecast>, locale : Locale, onDayClick: (DayForecast) -> Unit) {

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(dayForecasts) { day ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .blur(6.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable {
                            onDayClick(day)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val weatherComposition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(day.weatherDescription?.lottieFile ?: R.raw.weathersunny)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeatherText(day.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                        })
                        LottieAnimation(
                            modifier = Modifier.size(35.dp),
                            composition = weatherComposition,
                            iterations = LottieConstants.IterateForever
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    WeatherText("${formatTemperature(day.tempMax)} / ${formatTemperature(day.tempMin)}")

                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DailyForecastListPreview()
{
    val sampleData =
        listOf(
        DayForecast(
            date = "2025-11-07",
            dayOfWeek = DayOfWeek.SATURDAY,
            tempMax = 17.8,
            tempMin = 10.8,
            weatherCode = 3,
        ),
        DayForecast(
            date = "2025-11-08",
            dayOfWeek = DayOfWeek.SUNDAY,
            tempMax = 21.6,
            tempMin = 8.8,
            weatherCode = 45,
        ),
        DayForecast(
            date = "2025-11-09",
            dayOfWeek = DayOfWeek.MONDAY,
            tempMax = 22.3,
            tempMin = 12.1,
            weatherCode = 3,
        )
    )
    val locale = LocalConfiguration.current.locales[0]
    DailyForecastList(dayForecasts = sampleData, locale) { }
}
