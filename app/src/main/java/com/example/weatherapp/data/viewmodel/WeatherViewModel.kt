package com.example.weatherapp.data.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.weatherapp.R
import com.example.weatherapp.data.model.DayForecast
import com.example.weatherapp.data.model.WeatherDescription
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.model.WeatherUIData
import com.example.weatherapp.data.workers.WorkManagerController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

sealed class WeatherUIState {
    object Loading : WeatherUIState()
    data class Success(val weatherResponse: WeatherResponse) : WeatherUIState()
    data class Error(val message : String) : WeatherUIState()
}

class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)


    private val _hasFinePermission = MutableStateFlow(false)
    private val _hasCoarsePermission = MutableStateFlow(false)
    private val _hasLocationPermission = MutableStateFlow(false)
    private val _geoCity = MutableStateFlow<String?>(null)
    private val _weatherResponse = MutableStateFlow<WeatherResponse?>(null)
    private val _weatherUIData = MutableStateFlow(WeatherUIData())

    private val _isDay = MutableStateFlow(isDayNow())


    val isDay : StateFlow<Boolean> = _isDay

    private val _locale = MutableStateFlow(Locale.getDefault())


    val geoCity: StateFlow<String?> = _geoCity.asStateFlow()

    val hasFinePermission : StateFlow<Boolean> = _hasFinePermission.asStateFlow()
    val hasCoarsePermission : StateFlow<Boolean> = _hasCoarsePermission.asStateFlow()
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    val weatherResponse: StateFlow<WeatherResponse?> = _weatherResponse.asStateFlow()
    val weatherUIData: StateFlow<WeatherUIData> = _weatherUIData.asStateFlow()

    val locale: StateFlow<Locale> = _locale

    init {
        viewModelScope.launch {
            _weatherResponse.collect { weather ->
                if (weather != null) {
                    val daily = parseDailyForecasts(weather)
                    val currentDesc = getWeatherDescription(weather.current?.weatherCode, !_isDay.value)
                    _weatherUIData.value = WeatherUIData(
                        currentWeather = weather.current,
                        dailyForecasts = daily,
                        currentWeatherDescription = currentDesc
                    )
                } else {
                    _weatherUIData.value = WeatherUIData()
                }
            }
        }
    }



    val uiState : StateFlow<WeatherUIState> = _uiState.asStateFlow()

    private fun isDayNow(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        Log.d("isDayNow", hour.toString())
        return hour in 6..18
    }


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

    fun setLocale(locale: Locale) {
        _locale.value = locale
    }

    fun startLocationWorker(context : Context) {
        Log.d("WeatherViewModel", "Start chain of workers through WorkManagerController")
        val workRequest = WorkManagerController(context)
        workRequest.startLocationAndWeatherChain()
        observeWorkChain(context, workRequest, "weather_work_chain")
    }

    fun observeWorkChain(context: Context, controller: WorkManagerController, uniqueWorkName : String) {
        Log.d("WeatherViewModel", "Observe WorkManager for chain $uniqueWorkName")

        viewModelScope.launch {
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData("weather_work_chain")
                .asFlow()
                .collect { workInfos ->
                    val locationInfo = workInfos.find { it.id == controller.getLocationWorkId() }
                    locationInfo?.let {
                        Log.d("WeatherViewModel", "LocationWorker finished: state=${it.state}")
                        if (it.state.isFinished) {
                            if(it.state == WorkInfo.State.FAILED) {
                                controller.cancelAll()
                            }
                            controller.pruneAllFinishedWork()
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
                            if(it.state == WorkInfo.State.FAILED) {
                                controller.cancelAll()
                            }
                            controller.pruneAllFinishedWork()
                            val weatherJson = it.outputData.getString("weather_data")
                            val gson = Gson()
                            Log.d("WeatherViewModel", "Weather JSON from WeatherWorker: $weatherJson")
                            weatherJson?.let { json ->
                                val weatherResponseObj = gson.fromJson(json, WeatherResponse::class.java)
                                _uiState.value = WeatherUIState.Success(weatherResponseObj)
                                _weatherResponse.value = weatherResponseObj
                            }
                        }
                    }

                    if (workInfos.isEmpty()) {
                        Log.d("WeatherViewModel", "All workers removed!")
                    } else {
                        Log.d("WeatherViewModel", "Current workers: ${workInfos.size}")
                        workInfos.forEach { workInfo ->
                            Log.d("WeatherViewModel", "  - ${workInfo.id.toString().substring(0, 8)}: ${workInfo.state}")
                        }
                    }
                }
        }
    }

    fun getWeatherDescription(weatherCode: Int?, isNight : Boolean? = null): WeatherDescription {
        Log.d("getWeatherDescription", "isNight: ${isNight}")
        if(isNight == null || isNight == false) {
            return when (weatherCode) {
                0 -> WeatherDescription("Ясно", R.raw.weathersunny)
                1, 2 -> WeatherDescription("Переменная облачность", R.raw.weatherpartlycloudy)
                3 -> WeatherDescription("Пасмурно", R.raw.weatherwindy)
                45, 48 -> WeatherDescription("Туман", R.raw.weatherwindy)
                51, 61 -> WeatherDescription("Небольшой дождь", R.raw.weatherpartlyshower)
                63, 65 -> WeatherDescription("Сильный дождь", R.raw.rainyicon)
                71, 73 -> WeatherDescription("Снег", R.raw.weathersnow)
                95, 96 -> WeatherDescription("Гроза", R.raw.weatherstorm)
                else -> WeatherDescription("Неизвестно", null)
            }
        }
        else {
            return when (weatherCode) {
                0 -> WeatherDescription("Ясно", R.raw.weather_night)
                1, 2 -> WeatherDescription("Переменная облачность", R.raw.weather_cloudy_night)
                3 -> WeatherDescription("Пасмурно", R.raw.weather_cloudy_night)
                45, 48 -> WeatherDescription("Туман", R.raw.weatherwindy)
                51, 61 -> WeatherDescription("Небольшой дождь", R.raw.weather_rainy_night)
                63, 65 -> WeatherDescription("Сильный дождь", R.raw.rainyicon)
                71, 73 -> WeatherDescription("Снег", R.raw.weathersnow)
                95, 96 -> WeatherDescription("Гроза", R.raw.weatherstorm)
                else -> WeatherDescription("Неизвестно", null)
            }
        }
    }


   suspend fun parseDailyForecasts(weather : WeatherResponse) : List<DayForecast> = withContext(
       Dispatchers.Default) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd", locale.value)
            val daily = weather.daily ?: return@withContext emptyList()

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
                val description = getWeatherDescription(weatherCode)
                list.add(DayForecast(
                    date = dateStr,
                    dayOfWeek = dayOfWeek,
                    tempMax = tempMax,
                    tempMin = tempMin,
                    weatherCode = weatherCode,
                    weatherDescription = description
                ))
            }
            return@withContext list
        }
        else {
            TODO("VERSION.SDK_INT < O")
        }
    }
}