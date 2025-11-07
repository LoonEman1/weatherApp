package com.example.weatherapp.data.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.weatherapp.data.model.DayForecast
import com.example.weatherapp.data.model.GeoData
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.workers.LocationWorker
import com.example.weatherapp.data.workers.WorkManagerController
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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

    private val _locale = MutableStateFlow(Locale.getDefault())


    val geoCity: StateFlow<String?> = _geoCity.asStateFlow()

    val hasFinePermission : StateFlow<Boolean> = _hasFinePermission.asStateFlow()
    val hasCoarsePermission : StateFlow<Boolean> = _hasCoarsePermission.asStateFlow()
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    val weatherResponse: StateFlow<WeatherResponse?> = _weatherResponse
    val locale: StateFlow<Locale> = _locale


    val dailyForecasts: StateFlow<List<DayForecast>> = _weatherResponse.map { weather ->
        weather?.let { parseDailyForecasts(it) } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())



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

    fun updateUiState(uiState: WeatherUIState) {
        _uiState.value = uiState
    }

    fun updateWeather(newWeather: WeatherResponse) {
        _weatherResponse.value = newWeather
    }

    fun setLocale(locale: Locale) {
        _locale.value = locale
    }

    fun startLocationWorker(context : Context) {
        if(!isWorkerStarted) {
            Log.d("WeatherViewModel", "Start chain of workers through WorkManagerController")
            isWorkerStarted = true
            val workRequest = WorkManagerController(context)
            workRequest.startLocationAndWeatherChain()
            observeWorkChain(context, workRequest, "weather_work_chain")
        }
    }

    fun observeWorkChain(context: Context, controller: WorkManagerController, uniqueWorkName : String) {
        Log.d("WeatherViewModel", "Observe WorkManager for chain $uniqueWorkName")
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
                        val gson = Gson()

                        Log.d("WeatherViewModel", "Weather JSON from WeatherWorker: $weatherJson")
                        weatherJson?.let { json ->
                            val weatherResponseObj = gson.fromJson(json, WeatherResponse::class.java)
                            _uiState.value = WeatherUIState.Success(weatherResponseObj)
                        }
                    }
                }
            }
    }

    fun parseDailyForecasts(weather : WeatherResponse) : List<DayForecast>? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd", locale.value)
            val daily = weather.daily ?: return emptyList()

            val count = listOf(
                daily.temperatureMax.size,
                daily.temperatureMin.size,
                daily.time.size,
                daily.weatherCode.size
            ).minOrNull() ?: 0

            val list = mutableListOf<DayForecast>()

            for (i in 0 until count) {
                val dateStr = daily.time[i]
                val date = LocalDate.parse(dateStr, formatter)
                val dayOfWeek = date.dayOfWeek
                val tempMax = daily.temperatureMax[i]
                val tempMin = daily.temperatureMin[i]
                val weatherCode = daily.weatherCode[i]

                list.add(DayForecast(
                    date = dateStr,
                    dayOfWeek = dayOfWeek,
                    tempMax = tempMax,
                    tempMin = tempMin,
                    weatherCode = weatherCode
                ))
            }
            return list
        }
        else {
            TODO("VERSION.SDK_INT < O")
        }
    }
}