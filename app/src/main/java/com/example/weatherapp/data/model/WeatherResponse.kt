package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("timezone")
    val timezone: String,

    @SerializedName("current")
    val current: CurrentWeather?,

    @SerializedName("hourly")
    val hourly: HourlyWeather?,

    @SerializedName("daily")
    val daily: DailyWeather?
)