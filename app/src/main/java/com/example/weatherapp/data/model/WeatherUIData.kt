package com.example.weatherapp.data.model

data class WeatherUIData(
    val currentWeather: CurrentWeather? = null,
    val currentWeatherDescription : WeatherDescription? = null,
    val dailyForecasts: List<DayForecast> = emptyList(),

)
