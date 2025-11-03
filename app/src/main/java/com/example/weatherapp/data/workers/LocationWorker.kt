package com.example.weatherapp.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.weatherapp.data.location.GeoManager


class LocationWorker(
    context: Context, workerParameters: WorkerParameters
    ) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        val geoManager = GeoManager()

        val geoData = geoManager.updateLastKnownLocation(applicationContext)

        if (geoData == null) {
            Log.d("LocationWorker", "Work ${id}, GEODATA IS NULL")
            return Result.retry()
        }
        else {
            Log.d("LocationWorker", geoData.city)
            val outputData = workDataOf(
                "latitude" to geoData.latitude,
                "longitude" to geoData.longitude,
                "city" to geoData.city
            )
            return Result.success(outputData)
        }
    }
}