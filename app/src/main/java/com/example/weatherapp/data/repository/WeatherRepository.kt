package com.example.weatherapp.data.repository

import com.example.weatherapp.data.api.RetrofitClient
import com.example.weatherapp.data.model.WeatherResponse

class WeatherRepository {
    suspend fun getWeather( latitude: Double, longitude: Double) : Result<WeatherResponse> {
        return try {
            val response = RetrofitClient.weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude
            )
            Result.success(response)
        } catch (e : Exception) {
            Result.failure(e)
        }
    }
}