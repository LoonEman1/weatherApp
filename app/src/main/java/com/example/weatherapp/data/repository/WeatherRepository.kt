package com.example.weatherapp.data.repository

import com.example.weatherapp.data.api.RetrofitClient
import com.example.weatherapp.data.model.WeatherResponse

class WeatherRepository {
    suspend fun getWeather() : Result<WeatherResponse> {
        return try {
            val response = RetrofitClient.weatherApi.getWeather(
                latitude = 47.2222596,
                longitude = 39.7198736
            )
            Result.success(response)
        } catch (e : Exception) {
            Result.failure(e)
        }
    }
}