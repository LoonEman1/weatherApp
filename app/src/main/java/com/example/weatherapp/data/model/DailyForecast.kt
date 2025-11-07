package com.example.weatherapp.data.model

import java.time.DayOfWeek

data class DayForecast(
    val date : String,
    val dayOfWeek: DayOfWeek,
    val tempMax : Double,
    val tempMin : Double,
    val weatherCode : Int?,
)
