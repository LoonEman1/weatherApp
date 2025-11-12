package com.example.weatherapp.data.model


data class HourlyWeatherForecastUI(
    val time: List<String> = emptyList(),
    val temperature: List<Double> = emptyList(),
)
