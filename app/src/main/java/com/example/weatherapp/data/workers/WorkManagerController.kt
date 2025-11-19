package com.example.weatherapp.data.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.UUID

class WorkManagerController(private val context : Context) {

    private lateinit var locationWorkId: UUID
    private lateinit var uploadWorkId: UUID

    fun startLocationAndWeatherChain() {

        val locationWorker = OneTimeWorkRequestBuilder<LocationWorker>()
            .build()
        locationWorkId = locationWorker.id

        val uploadWorker = OneTimeWorkRequestBuilder<WeatherWorker>()
            .build()
        uploadWorkId = uploadWorker.id

        WorkManager.getInstance(context)
            .beginUniqueWork("weather_work_chain", ExistingWorkPolicy.REPLACE, locationWorker)
            .then(uploadWorker)
            .enqueue()
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelUniqueWork("weather_work_chain")
    }

    fun pruneAllFinishedWork() {
        WorkManager.getInstance(context).pruneWork()
    }

    fun getLocationWorkId(): UUID = locationWorkId
    fun getUploadWorkId(): UUID = uploadWorkId
}


