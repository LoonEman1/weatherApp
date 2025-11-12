import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.R
import com.example.weatherapp.data.model.CurrentWeather
import com.example.weatherapp.data.model.WeatherDescription
import com.example.weatherapp.data.model.WeatherUIData
import com.example.weatherapp.ui.theme.view.common.WeatherText
import com.example.weatherapp.ui.theme.view.common.formatTemperature
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CurrentWeatherCard(
    weatherUIData: WeatherUIData,
    city : String
) {
    Log.d("CurrentWeatherDescription", weatherUIData.currentWeatherDescription.toString())
    val weatherComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(weatherUIData.currentWeatherDescription?.lottieFile ?: R.raw.weathersunny)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            WeatherText(city, fontSize = 22.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
            ) {
                val weatherInfo = listOf(
                    Pair(formatTemperature(weatherUIData.currentWeather?.temperature), null),
                    Pair(
                        "${weatherUIData.currentWeather?.windSpeed ?: "--"} км/ч",
                        R.raw.wind_gust
                    ),
                    Pair("${weatherUIData.currentWeather?.humidity} %" ?: "0%", R.raw.humidity),
                    Pair(weatherUIData.currentWeatherDescription?.description ?: "--", null)
                )

                weatherInfo.forEach { (value, rawLottie) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        if (rawLottie != null) {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    rawLottie
                                )
                            )
                            LottieAnimation(
                                modifier = Modifier
                                    .size(40.dp),
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                speed = 0.5f
                            )
                        } else {
                            Spacer(
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        WeatherText(value)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            LottieAnimation(
                modifier = Modifier
                    .size(140.dp),
                composition = weatherComposition,
                iterations = LottieConstants.IterateForever
            )
        }
    }
}


@Composable
fun DayCard(
    rawLottie : Int = R.raw.weathersunny, city : String, dayOfWeek: String, date : String
) {
    val weatherComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(rawLottie)
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
    ) {
        val dayInfo = listOf(
            Pair(city, null),
            Pair(dayOfWeek, null),
            Pair(date, 14.sp)
        )

        dayInfo.forEach { (value, fontSize) ->
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
            ) {
                WeatherText(value, fontSize)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        LottieAnimation(
            modifier = Modifier
                .size(140.dp),
            composition = weatherComposition,
            iterations = LottieConstants.IterateForever
        )
    }
    Spacer(modifier = Modifier.height(20.dp))

}


@Preview(showBackground = true)
@Composable
fun CurrentWeatherCardPreview() {
    val testWeather = CurrentWeather(
        time = "2025-11-11T09:00:00",
        temperature = 19.8,
        humidity = 56,
        weatherCode = 3,
        windSpeed = 8.5
    )
    val testDescription = WeatherDescription(
        description = "Пасмурно",
        lottieFile = R.raw.weathersunny
    )
    val weatherUIData = WeatherUIData(
        currentWeather = testWeather,
        currentWeatherDescription = testDescription,
        dailyForecasts = emptyList()
    )
    val city = "San Francisco"
    CurrentWeatherCard(
        weatherUIData = weatherUIData, city =  city
    )
}

@Preview(showBackground = true)
@Composable
fun DayCardPreview() {
    val city = "San Francisco"
    val rawFile = R.raw.weathersunny
    val date = "2025-11-12"

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val localDate = LocalDate.parse(date, formatter)
    val dayOfWeek = localDate.dayOfWeek.toString()
    DayCard(rawFile, city, dayOfWeek, date)
}