package com.example.weatherapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.weatherapp.data.database.WeatherDatabase
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.example.weatherapp.MainActivity


class WeatherGlanceWidget : GlanceAppWidget(0) {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherData = getLatestWeatherFromDb(context)

        provideContent {
            WeatherWidgetContent(weatherData)
        }
    }

    private suspend fun getLatestWeatherFromDb(context: Context): Map<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val weatherDao = WeatherDatabase.getDatabase(context).weatherDao()
                val latestWeather = weatherDao.getLatestWeather()

                if (latestWeather != null) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(latestWeather.weatherDataJson, JsonObject::class.java)

                    val current = jsonObject.getAsJsonObject("current")
                    val temp = current?.get("temperature_2m")?.asDouble?.toInt() ?: 0
                    val weatherCode = current?.get("weather_code")?.asInt ?: 0
                    val condition = getWeatherDescription(weatherCode)

                    mapOf(
                        "city" to latestWeather.city,
                        "temperature" to "$temp°C",
                        "condition" to condition,
                        "weatherCode" to weatherCode.toString()
                    )
                } else {
                    mapOf(
                        "city" to "Нет данных",
                        "temperature" to "—",
                        "condition" to "—",
                        "weatherCode" to "0"
                    )
                }
            } catch (e: Exception) {
                Log.e("WeatherWidget", "Error reading from DB: ${e.message}")
                mapOf(
                    "city" to "Ошибка",
                    "temperature" to "—",
                    "condition" to "—",
                    "weatherCode" to "0"
                )
            }
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Ясно"
            1, 2 -> "Облачно"
            3 -> "Пасмурно"
            45, 48 -> "Туман"
            51, 53, 55 -> "Морось"
            61, 63, 65 -> "Дождь"
            71, 73, 75 -> "Снег"
            77 -> "Снег"
            80, 81, 82 -> "Ливень"
            85, 86 -> "Снег с дождём"
            95, 96, 99 -> "Гроза"
            else -> "Неизвестно"
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun WeatherWidgetContent(weatherData: Map<String, String>){
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    ColorProvider(Color(0x59000000))
                )
                .clickable(
                    onClick = actionStartActivity(
                        Intent(LocalContext.current, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = weatherData["city"] ?: "—",
                    modifier = GlanceModifier.padding(bottom = 4.dp),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = weatherData["temperature"] ?: "—",
                    modifier = GlanceModifier.padding(bottom = 4.dp),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = weatherData["condition"] ?: "—",
                    modifier = GlanceModifier.padding(bottom = 4.dp),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFBBBBBB)),
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}
