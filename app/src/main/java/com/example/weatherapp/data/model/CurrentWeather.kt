package com.example.weatherapp.data.model

import android.health.connect.datatypes.units.Temperature
import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @SerializedName("time")
    val time : String,

    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("relative_humidity_2m")
    val humidity: Int,

    @SerializedName("weather_code")
    val weatherCode: Int,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double
)