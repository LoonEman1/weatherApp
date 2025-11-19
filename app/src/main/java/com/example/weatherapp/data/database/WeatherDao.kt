package com.example.weatherapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather_cache WHERE id = :locationId")
    suspend fun getWeather(locationId: String): WeatherEntity?

    @Query("SELECT * FROM weather_cache WHERE latitude = :lat AND longitude = :lon")
    suspend fun getWeatherByCoordinates(lat: Double, lon: Double): WeatherEntity?

    @Query("SELECT * FROM weather_cache WHERE city = :cityName LIMIT 1")
    suspend fun getWeatherByCity(cityName: String): WeatherEntity?

    @Query("DELETE FROM weather_cache WHERE timestamp < :expireTime")
    suspend fun deleteExpiredCache(expireTime: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAll()
}