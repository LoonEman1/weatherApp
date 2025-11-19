package com.example.weatherapp.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.weatherapp.data.database.WeatherDatabase
import com.example.weatherapp.data.location.GeoManager
import com.example.weatherapp.data.model.GeoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationWorker(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        Log.d("LocationWorker", "runAttemptCount: $runAttemptCount")

        return withContext(Dispatchers.IO) {
            if (runAttemptCount >= 5) {
                Log.e("LocationWorker", "aximum retry attempts reached")
                return@withContext Result.failure()
            }

            val hasInternet = isNetworkAvailable(applicationContext)
            Log.d("LocationWorker", "Network available: $hasInternet")

            if (hasInternet) {
                val geoManager = GeoManager()
                val geoData = geoManager.updateLastKnownLocation(applicationContext)

                if (geoData == null) {
                    Log.w("LocationWorker", "GeoData is null, retrying...")
                    return@withContext Result.retry()
                }

                if (geoData.city == "Unknown(no internet)" || geoData.city == "Unknown") {
                    Log.w("LocationWorker", " City unknown, retrying...")
                    return@withContext Result.retry()
                }

                Log.d("LocationWorker", "Got fresh data: ${geoData.city}")
                val outputData = workDataOf(
                    "latitude" to geoData.latitude,
                    "longitude" to geoData.longitude,
                    "city" to geoData.city
                )
                return@withContext Result.success(outputData)

            } else {
                Log.d("LocationWorker", "No internet, trying to get data from cache")

                val cachedData = getCityFromDatabase()

                if (cachedData != null) {
                    Log.d("LocationWorker", "Using cached data: ${cachedData.city}")
                    val outputData = workDataOf(
                        "latitude" to cachedData.latitude,
                        "longitude" to cachedData.longitude,
                        "city" to cachedData.city
                    )
                    return@withContext Result.success(outputData)
                } else {
                    Log.w("LocationWorker", "No cached data available, retrying...")
                    return@withContext Result.retry()
                }
            }
        }
    }

    private suspend fun getCityFromDatabase(): GeoData? {
        return withContext(Dispatchers.IO) {
            try {
                val weatherDao = WeatherDatabase.getDatabase(applicationContext).weatherDao()

                val cached = weatherDao.getLatestWeather()

                if (cached != null) {
                    Log.d("LocationWorker", "Found cached location: ${cached.city} (${cached.latitude}, ${cached.longitude})")
                    GeoData(
                        latitude = cached.latitude,
                        longitude = cached.longitude,
                        city = cached.city
                    )
                } else {
                    Log.d("LocationWorker", "No cached data in database")
                    null
                }
            } catch (e: Exception) {
                Log.e("LocationWorker", "Failed to get cached data: ${e.message}")
                null
            }
        }
    }
}
