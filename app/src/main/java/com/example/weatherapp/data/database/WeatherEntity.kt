package com.example.weatherapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherapp.data.model.WeatherResponse
import com.google.gson.Gson

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey
    val id : String,
    val city : String,
    val latitude : Double,
    val longitude : Double,
    val weatherDataJson : String,
    val timestamp : Long,
)

fun WeatherResponse.toEntity(city : String) : WeatherEntity {
    val gson = Gson()
    return WeatherEntity(
        id = "${latitude}_${longitude}",
        city = city,
        latitude = latitude,
        longitude = longitude,
        weatherDataJson = gson.toJson(this),
        timestamp = System.currentTimeMillis()
    )
}

fun WeatherEntity.toWeatherResponse(): WeatherResponse {
    val gson = Gson()
    return gson.fromJson(weatherDataJson, WeatherResponse::class.java)
}
