package com.example.weatherapp.data.repository

import android.content.Context
import android.util.Log
import com.example.weatherapp.data.api.RetrofitClient
import com.example.weatherapp.data.database.WeatherDatabase
import com.example.weatherapp.data.database.toEntity
import com.example.weatherapp.data.database.toWeatherResponse
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.workers.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val context : Context) {

    private val weatherDao = WeatherDatabase.getDatabase(context).weatherDao()
    private val cacheExpirationTime = 24 * 60 * 60 * 1000L

    suspend fun getWeather(latitude: Double, longitude: Double, city : String) : Result<WeatherResponse> {
        return try {

            val hasInternet = isNetworkAvailable(context.applicationContext)
            Log.d("WeatherRepository", "Network check: internet=$hasInternet")

            if(!hasInternet) {
                val cachedWeather = weatherDao.getWeatherByCity(city)

                if (cachedWeather != null) {
                    val age = System.currentTimeMillis() - cachedWeather.timestamp

                    if (age < cacheExpirationTime) {
                        Log.d("WeatherRepository", "Returning cached data (age: ${age / 1000}s)")
                        return Result.success(cachedWeather.toWeatherResponse())
                    } else {
                        Log.d("WeatherRepository", "Cache expired (age: ${age / 1000}s), fetching new data")
                    }
                }
                Log.d("WeatherRepository", "No cache and internet")
                return Result.failure(Exception("No cache and internet"))
            }

            val response = RetrofitClient.weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude
            )
            weatherDao.insertWeather(response.toEntity(city))
            Log.d("WeatherRepository", "Weather data cached successfully")
            Result.success(response)
        } catch (e : Exception) {
            val cachedWeather = weatherDao.getWeatherByCoordinates(latitude, longitude)
            if (cachedWeather != null) {
                Log.d("WeatherRepository", "API failed, returning stale cache")
                Result.success(cachedWeather.toWeatherResponse())
            }
            Result.failure(e)
        }
    }

    suspend fun cleanupExpiredCache() = withContext(Dispatchers.IO) {
        val expireTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        weatherDao.deleteExpiredCache(expireTime)
        Log.d("WeatherRepository", "Expired cache cleaned up")
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        weatherDao.clearAll()
        Log.d("WeatherRepository", "All cache cleared")
    }
}