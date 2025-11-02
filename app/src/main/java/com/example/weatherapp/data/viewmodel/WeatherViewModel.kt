package com.example.weatherapp.data.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.weatherapp.data.model.GeoData
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.workers.LocationWorker
import com.example.weatherapp.data.workers.WorkManagerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class WeatherUIState {
    object Loading : WeatherUIState()
    data class Success(val weatherResponse: WeatherResponse) : WeatherUIState()
    data class Error(val message : String) : WeatherUIState()
}

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)


    private val _hasFinePermission = MutableStateFlow(false)
    private val _hasCoarsePermission = MutableStateFlow(false)
    private val _hasLocationPermission = MutableStateFlow(false)
    private val _geoCity = MutableStateFlow<String?>(null)
    private val _weatherResponse = MutableStateFlow<WeatherResponse?>(null)


    val weatherResponse: StateFlow<WeatherResponse?> = _weatherResponse.asStateFlow()
    val geoCity: StateFlow<String?> = _geoCity.asStateFlow()

    val hasFinePermission : StateFlow<Boolean> = _hasFinePermission.asStateFlow()
    val hasCoarsePermission : StateFlow<Boolean> = _hasCoarsePermission.asStateFlow()
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private var isWorkerStarted = false



    val uiState : StateFlow<WeatherUIState> = _uiState.asStateFlow()

    fun updateFinePermission(fineGranted: Boolean) {
        _hasFinePermission.value = fineGranted
    }

    fun updateCoarsePermission(coarseGranted: Boolean) {
        _hasCoarsePermission.value = coarseGranted

    }

    fun updatePermissionStatus(granted: Boolean) {
        _hasLocationPermission.value = granted
    }

    fun updatePermissionStatus(coarseStatus: Boolean, fineStatus : Boolean) {
        _hasLocationPermission.value = coarseStatus or fineStatus
    }

    fun startLocationWorker(context : Context) {
        if(!isWorkerStarted) {
            Log.d("WeatherViewModel", "Запуск цепочки воркеров через WorkManagerController")
            isWorkerStarted = true
            val workRequest = WorkManagerController(context)
            workRequest.startLocationAndWeatherChain()
            observeWorkChain(context, workRequest, "weather_work_chain")
        }
    }

    fun observeWorkChain(context: Context, controller: WorkManagerController, uniqueWorkName : String) {
        Log.d("WeatherViewModel", "Наблюдение за WorkManager цепочкой $uniqueWorkName")
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData("weather_work_chain")
            .observeForever { workInfos ->
                val locationInfo = workInfos.find { it.id == controller.getLocationWorkId() }
                locationInfo?.let {
                    Log.d("WeatherViewModel", "LocationWorker finished: state=${it.state}")
                    if (it.state.isFinished) {
                        val city = it.outputData.getString("city")
                        if (city != null) {
                            _geoCity.value = city
                        }
                    }
                }

                val weatherInfo = workInfos.find { it.id == controller.getUploadWorkId() }
                weatherInfo?.let {
                    Log.d("WeatherViewModel", "WeatherWorker finished: state=${it.state}")
                    if (it.state.isFinished) {
                        val weatherJson = it.outputData.getString("weather_data")
                        Log.d("WeatherViewModel", "Weather JSON из WeatherWorker: $weatherJson")
                        weatherJson?.let { json ->
                            _weatherResponse.value = weatherResponse.value
                        }
                    }
                }
            }
    }
}