package com.example.weatherapp.ui.theme.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.R
import com.example.weatherapp.data.model.DayForecast
import com.example.weatherapp.data.viewmodel.WeatherUIState
import com.example.weatherapp.data.viewmodel.WeatherViewModel

import java.time.DayOfWeek

@Composable
fun WeatherScreen(navController: NavHostController) {
    val viewModel: WeatherViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    viewModel.setLocale(locale)


    val hasFinePermission by viewModel.hasFinePermission.collectAsState()
    val hasCoarsePermissions by viewModel.hasCoarsePermission.collectAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val city by viewModel.geoCity.collectAsState()
    val weatherUIData by viewModel.weatherUIData.collectAsState()

    val isDay by viewModel.isDay.collectAsState()

    val context = LocalContext.current




    if (!hasFinePermission || !hasCoarsePermissions) {
        val fineGranted = ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.updateFinePermission(fineGranted)
        viewModel.updateCoarsePermission(coarseGranted)
        viewModel.updatePermissionStatus(fineGranted, coarseGranted)
    }

    val backgroundRes = if (isDay) {
        R.raw.background_fullscreen_day
    } else {
        R.raw.background_fullscreen_night
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(backgroundRes))

    val requestPermissions = PermissionRequester { granted ->
        viewModel.updatePermissionStatus(granted)
    }


    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            requestPermissions()
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if(hasLocationPermission) {
            viewModel.startLocationWorker(context)
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
            /*Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(
                        colors = listOf(Color(0xFF2badff), Color(0xFF2aacff), Color(0xFF2cadff), Color(0xFF71c8ff), Color(0xFF72c9ff), Color(0xFF70c8ff))
                    ))
            )*/
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
                        val temperature = weatherUIData.currentWeather?.temperature
                        val windSpeed = weatherUIData.currentWeather?.windSpeed
                        val description = weatherUIData.currentWeatherDescription

                        val weatherComposition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(description?.lottieFile ?: R.raw.weathersunny)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                             contentAlignment = Alignment.Center
                        )
                        {
                            WeatherText("Прогноз погоды на сегодня в городе \n ${city}:")
                        }
                        Spacer(modifier = Modifier.padding(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                WeatherText("$temperature °C")
                                Spacer(modifier = Modifier.height(4.dp))
                                WeatherText("༄ $windSpeed км/ч")
                                Spacer(modifier = Modifier.height(4.dp))
                                WeatherText(description?.description.toString())
                            }
                            LottieAnimation(
                                modifier = Modifier
                                    .size(140.dp),
                                composition = weatherComposition,
                                iterations = LottieConstants.IterateForever
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DailyForecastList(weatherUIData.dailyForecasts) { }

                    }
                }
            }
        }
    }
}


@Composable
fun DailyForecastList(dayForecasts: List<DayForecast>, onDayClick: () -> Unit) {

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
                    modifier = Modifier.padding(16.dp),
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
                        WeatherText(day.dayOfWeek.toString())
                        LottieAnimation(
                            modifier = Modifier.size(35.dp),
                            composition = weatherComposition,
                            iterations = LottieConstants.IterateForever
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    WeatherText("${day.tempMax} °C / ${day.tempMin} °C")

                }
            }
        }
    }
}

@Composable
fun WeatherText(text : String) {
    Text(
        text = text,
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif,
        color = Color.White,
        fontWeight = FontWeight.Black,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.8f),
                offset = Offset(2f, 2f),
                blurRadius = 6f
            )
        )
    )
}

@Composable
fun PermissionRequester(onPermissionResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onPermissionResult(granted)
    }

    return { launcher.launch((arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )))}
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview()
{
    val navController = rememberNavController()
    WeatherScreen(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun DailyForecastListPreview()
{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
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
        DailyForecastList(dayForecasts = sampleData) { }
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}
