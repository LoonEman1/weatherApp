package com.example.weatherapp.ui.theme.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.R
import com.example.weatherapp.data.viewmodel.WeatherViewModel

@Composable
fun DayDetails(navController: NavHostController, date: String?, viewModel: WeatherViewModel) {
    if (date == null) {
        Text("Нет даты")
        return
    }

    LaunchedEffect(date) {
        viewModel.setHourlyForecast(date)
    }

    val hourlyWeatherUI by viewModel.hourlyWeatherUI.collectAsState()
    val weatherUIData by viewModel.weatherUIData.collectAsState()
    val backgroundRes by viewModel.backgroundRes.collectAsState()


    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(backgroundRes)
    )

    Scaffold { innerPadding ->
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
        Column {
            Text("Дата: $date")
            if (hourlyWeatherUI.time.isNotEmpty()) {
                hourlyWeatherUI.time.forEachIndexed { idx, timeStr ->
                    val temp = hourlyWeatherUI.temperature.getOrNull(idx)
                    val precip = hourlyWeatherUI.precipitation.getOrNull(idx)
                    Text("$timeStr: Темп: $temp°C, Осадки: $precip мм")
                }
            } else {
                Text("Данные не найдены для выбранного дня")
            }
        }
            }
    }
}

@Preview
@Composable
fun PreviewDayDetails() {
    val date = "2025"
    val navController = rememberNavController()
    Text("Дата: $date")
}