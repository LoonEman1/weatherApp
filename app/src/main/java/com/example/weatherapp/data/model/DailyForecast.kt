package com.example.weatherapp.data.model

import com.example.weatherapp.R
import java.time.DayOfWeek

data class DayForecast(
    val date : String,
    val dayOfWeek: DayOfWeek,
    val tempMax : Double,
    val tempMin : Double,
    val weatherCode : Int = R.raw.weathersunny,
    val weatherDescription: WeatherDescription? = null
)
