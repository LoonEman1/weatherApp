package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class HourlyWeather (
    @SerializedName("time")
    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperature: List<Double>,

    @SerializedName("precipitation")
    val precipitation: List<Double>
)