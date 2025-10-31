package com.example.weatherapp.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.GeoData
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WeatherUIState {
    object Loading : WeatherUIState()
    data class Success(val weatherResponse: WeatherResponse) : WeatherUIState()
    data class Error(val message : String) : WeatherUIState()
}

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)

    private val _geoDataState = MutableStateFlow<GeoData?>(null)

    private val _hasFinePermission = MutableStateFlow<Boolean>(false)
    private val _hasCoarsePermission = MutableStateFlow<Boolean>(false)
    private val _hasLocationPermission = MutableStateFlow(false)

    val hasFinePermission : StateFlow<Boolean> = _hasFinePermission
    val hasCoarsePermission : StateFlow<Boolean> = _hasCoarsePermission
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission


    val geoDataState : StateFlow<GeoData?> = _geoDataState

    val uiState : StateFlow<WeatherUIState> = _uiState

    fun loadWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUIState.Loading
            val result = repository.getWeather(latitude, longitude)

            _uiState.value = result.fold(
                onSuccess = { WeatherUIState.Success(it) },
                onFailure = { WeatherUIState.Error(it.message ?: "Error ") }
            )
        }
    }

    fun updateLocation(geoData : GeoData?) {
        _geoDataState.value = geoData
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



}