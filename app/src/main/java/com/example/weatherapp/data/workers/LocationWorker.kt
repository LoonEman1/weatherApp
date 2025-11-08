package com.example.weatherapp.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.weatherapp.data.location.GeoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LocationWorker(
    context: Context, workerParameters: WorkerParameters
    ) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO)
        {
            if (runAttemptCount >= 5) {
                Log.d("LocationWorker", "Maximum retry attempts reached, giving up")
                return@withContext Result.failure()
            }
            val geoManager = GeoManager()

            val geoData = geoManager.updateLastKnownLocation(applicationContext)

            if (geoData == null || geoData.city == "Unknown(no internet)") {
                Log.d("LocationWorker", "Work ${id}, GEODATA IS NULL")
                return@withContext Result.retry()
            }
            else {
                Log.d("LocationWorker", geoData.city)
                val outputData = workDataOf(
                    "latitude" to geoData.latitude,
                    "longitude" to geoData.longitude,
                    "city" to geoData.city
                )
                Result.success(outputData)
            }
        }
    }
}