package com.example.weatherapp.ui.theme.view

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.R
import com.example.weatherapp.data.model.WeatherInfo
import com.example.weatherapp.data.viewmodel.WeatherUIState
import com.example.weatherapp.data.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(navController: NavHostController) {
    val viewModel: WeatherViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cloudsbackground))

    LaunchedEffect(Unit) {
        viewModel.loadWeather()
    }

    Scaffold(
    ) { innerPadding ->
        val padding = innerPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(
                        colors = listOf(Color(0xFF2badff), Color(0xFF2aacff), Color(0xFF2cadff), Color(0xFF71c8ff), Color(0xFF72c9ff), Color(0xFF70c8ff))
                    ))
            )
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState) {
                    is WeatherUIState.Loading -> {
                        Text(
                            text = "Загрузка данных...",
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

                    is WeatherUIState.Error -> {
                        val message = (uiState as WeatherUIState.Error).message
                        Text(
                            text = "Ошибка: $message",
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

                    is WeatherUIState.Success -> {
                        val weather = (uiState as WeatherUIState.Success).weatherResponse
                        val temperature = weather.current?.temperature
                        val windSpeed = weather.current?.windSpeed
                        val weatherCode = weather.current?.weatherCode
                        val descriptionCode = getWeatherDescription(weatherCode)

                        val weatherComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(descriptionCode.lottieFile ?: R.raw.weathersunny))
                        val location = "Ростов-на-Дону"

                        Text(
                            text = "Прогноз погоды на сегодня в городе \n $location:",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(),
                            fontSize = 22.sp,
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
                        Spacer(modifier = Modifier.padding(16.dp))
                        Text(
                            text = "Температура: $temperature °C",
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
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Скорость ветра: $windSpeed км/ч",
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
                        Spacer(modifier = Modifier.padding(4.dp))

                        Text(
                            text = "Состояние: ${descriptionCode.description}",
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
                        LottieAnimation(
                            composition = weatherComposition,
                            iterations = LottieConstants.IterateForever,
                        )
                    }
                }
            }
        }
    }
}

fun getWeatherDescription(weatherCode: Int?): WeatherInfo {
    return when (weatherCode) {
        0 -> WeatherInfo("Ясно", R.raw.weathersunny)
        1, 2 -> WeatherInfo("Переменная облачность",R.raw.weatherpartlycloudy)
        3 -> WeatherInfo("Пасмурно", R.raw.weatherwindy)
        45, 48 -> WeatherInfo("Туман", R.raw.weatherwindy)
        51, 61 -> WeatherInfo("Небольшой дождь",R.raw.weatherpartlyshower)
        63, 65 -> WeatherInfo("Сильный дождь", R.raw.rainyicon)
        71, 73 -> WeatherInfo("Снег",R.raw.weathersnow)
        95, 96 -> WeatherInfo("Гроза", R.raw.weatherstorm)
        else -> WeatherInfo("Неизвестно", null)
    }
}

@Composable
@Preview
fun ShowWeatherScreen()
{
    val navController = rememberNavController()
    WeatherScreen(navController)
}