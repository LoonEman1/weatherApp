package com.example.weatherapp.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val uiState : StateFlow<WeatherUIState> = _uiState

    fun loadWeather() {
        viewModelScope.launch {
            _uiState.value = WeatherUIState.Loading
            val result = repository.getWeather()

            _uiState.value = result.fold(
                onSuccess = { WeatherUIState.Success(it) },
                onFailure = { WeatherUIState.Error(it.message ?: "Error ") }
            )
        }
    }
}