package com.example.weatherapp.data.database

import androidx.room.Entity
import com.example.weatherapp.data.model.WeatherResponse
import com.google.gson.Gson

@Entity(tableName = "weather_cache",
    primaryKeys = ["city", "latitudeInt", "longitudeInt"]
)
data class WeatherEntity(
    val city : String,
    val latitude : Double,
    val longitude : Double,
    val latitudeInt : Int,
    val longitudeInt : Int,
    val weatherDataJson : String,
    val timestamp : Long,
)

fun WeatherResponse.toEntity(city : String) : WeatherEntity {
    val gson = Gson()
    return WeatherEntity(
        city = city,
        latitude = latitude,
        longitude = longitude,
        latitudeInt = latitude.toInt(),
        longitudeInt = longitude.toInt(),
        weatherDataJson = gson.toJson(this),
        timestamp = System.currentTimeMillis()
    )
}

fun WeatherEntity.toWeatherResponse(): WeatherResponse {
    val gson = Gson()
    return gson.fromJson(weatherDataJson, WeatherResponse::class.java)
}
