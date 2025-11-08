package com.example.weatherapp.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.weatherapp.data.repository.WeatherRepository
import com.google.gson.Gson

class WeatherWorker(context : Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        Log.d("WeatherWorker", "WeatherWorker runAttemptCount $runAttemptCount")

        if (runAttemptCount >= 5) {
            Log.d("WeatherWorker", "Maximum retry attempts reached, giving up")
            return Result.failure()
        }

        val lat = inputData.getDouble("latitude", 0.0)
        val lon = inputData.getDouble("longitude", 0.0)

        val success = WeatherRepository().getWeather(lat, lon)
        return if (success.isSuccess) {

            val weatherResponse = success.getOrNull()
            val gson = Gson()
            val weatherJson = gson.toJson(weatherResponse)
            Log.d("WeatherWorker", weatherJson)
            val output = Data.Builder()
                .putString("weather_data", weatherJson)
                .build()
            Result.success(output)
        } else {
            Log.d("WeatherWorker", "Failed to get weather: ${success.exceptionOrNull()}")
            Result.retry()
        }
    }
}