package com.example.weatherapp.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.weatherapp.data.repository.WeatherRepository

class WeatherWorker(context : Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val lat = inputData.getDouble("latitude", 0.0)
        val lon = inputData.getDouble("longitude", 0.0)

        val success = WeatherRepository().getWeather(lat, lon)
        return if (success.isSuccess) {
            Log.d("WeatherWorker", success.getOrNull().toString())
            val output = Data.Builder()
                .putString("weather_data", success.getOrNull()?.toString())
                .build()
            Result.success(output)
        } else {
            Log.d("WeatherWorker", "Failed to get weather: ${success.exceptionOrNull()}")
            Result.retry()
        }
    }
}