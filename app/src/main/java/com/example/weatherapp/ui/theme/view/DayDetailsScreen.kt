package com.example.weatherapp.ui.theme.view

import DayCard
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.R
import com.example.weatherapp.data.model.HourlyWeatherForecastUI
import com.example.weatherapp.data.viewmodel.WeatherViewModel
import com.example.weatherapp.ui.theme.view.common.WeatherText
import com.example.weatherapp.ui.theme.view.common.formatTemperature
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private val ITEM_WIDTH = 80.dp
val chartHeight = 140.dp
val rowHeight = 80.dp
val columnHeight = chartHeight + rowHeight + 50.dp


@Composable
fun DayDetails(navController: NavHostController, date: String?, viewModel: WeatherViewModel,
               dayOfWeek: String, rawFile : Int, city : String) {
    if (date == null) {
        WeatherText("Нет даты")
        return
    }

    LaunchedEffect(date) {
        viewModel.setHourlyForecast(date)
    }

    val hourlyWeatherUI by viewModel.hourlyWeatherUI.collectAsState()
    val weatherUIData by viewModel.weatherUIData.collectAsState()
    val backgroundRes by viewModel.backgroundRes.collectAsState()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(backgroundRes))


    Scaffold { innerPadding ->
        val padding = innerPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.FillBounds,
                speed = 0.6f
            )

            Column {
                Log.d("DayDetailsScreen", rawFile.toString())
                DayCard(
                    rawLottie = rawFile,
                    city = city,
                    dayOfWeek = dayOfWeek,
                    date = date
                )
                if (hourlyWeatherUI.time.isNotEmpty()) {
                    HourlyTemperatureChart(hourlyWeatherUI)
                } else {
                    WeatherText("Данные не найдены для выбранного дня")
                }
            }
        }
    }
}

@Composable
fun HourlyTemperatureChart(hourlyWeatherUI: HourlyWeatherForecastUI) {
    if (hourlyWeatherUI.time.isEmpty()) return

    val itemCount = hourlyWeatherUI.time.size
    val totalWidth = ITEM_WIDTH * itemCount

    val chartEntries = remember(hourlyWeatherUI) {
        hourlyWeatherUI.temperature.mapIndexed { index, temperature ->
            entryOf(index.toFloat(), temperature.toFloat())
        }
    }

    val times = remember(hourlyWeatherUI) {
        hourlyWeatherUI.time.map { timeString ->
            timeString.substringAfter("T").take(2)
        }
    }

    val bottomAxisValueFormatter = remember(times) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.toInt()
            if (index >= 0 && index < times.size) times[index] else ""
        }
    }

    val startAxisValueFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            "${value.toInt()}°"
        }
    }

    val chartEntryModelProducer = remember(chartEntries) {
        ChartEntryModelProducer(chartEntries)
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(chartEntries) {
        chartEntryModelProducer.setEntries(chartEntries)
    }

    val pointComponent = ShapeComponent(
        shape = Shapes.pillShape,
        color = Color.Red.toArgb(),
        strokeWidthDp = 3f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        WeatherText(
            text = "Почасовой прогноз погоды:",
            fontSize = 24.sp
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Box(
        modifier = Modifier
            .width(totalWidth)
            .height(columnHeight)
            .horizontalScroll(scrollState)
    ) {
        repeat(times.size) { index ->
            Box(
                modifier = Modifier
                    .offset(x = ITEM_WIDTH * index)
                    .width(ITEM_WIDTH - 5.dp)
                    .height(columnHeight - 60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .blur(6.dp)
            )
            Box(
                modifier = Modifier
                    .offset(x = ITEM_WIDTH * index)
                    .width(ITEM_WIDTH - 5.dp)
                    .height(rowHeight - 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .blur(6.dp)
            )
        }
        Column {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                HourlyTimeLabels(
                    hourlyWeatherUI = hourlyWeatherUI,
                    itemWidth = ITEM_WIDTH,
                    totalWidth = totalWidth,
                )
            }
            Spacer(
                modifier = Modifier.height(50.dp)
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            LineChart.LineSpec(
                                lineColor = Color.Red.toArgb(),
                                lineThicknessDp = 3f,
                                point = pointComponent
                            )
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        axis = null,
                        guideline = null,
                        tick = null,
                        label = null,
                        valueFormatter = startAxisValueFormatter,
                    ),
                    bottomAxis = rememberBottomAxis(
                        axis = null,
                        guideline = null,
                        tick = null,
                        label = null,
                        valueFormatter = bottomAxisValueFormatter,
                    ),
                    modifier = Modifier
                        .width(totalWidth)
                        .height(140.dp)
                        .padding(vertical = 4.dp),
                    runInitialAnimation = true
                )
            }
        }
    }
}

@Composable
fun HourlyTimeLabels(
    hourlyWeatherUI: HourlyWeatherForecastUI,
    itemWidth: Dp,
    totalWidth: Dp,
) {


    val times = remember(hourlyWeatherUI) {
        hourlyWeatherUI.time.map { timeString ->
            val hour = timeString.substringAfter("T").take(2)

            if(hour.startsWith("0")) {
            hour.substring(1)
            } else {
                hour
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        LazyRow(modifier = Modifier.width(totalWidth)) {
            items(times.size) { index ->
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WeatherText(
                            text = times[index],
                            fontSize = 19.sp
                        )
                        WeatherText(
                            text = formatTemperature(hourlyWeatherUI.temperature[index]),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDayDetails() {
    val date = "2025-11-12"
    val city = "San Francisco"

    val navController = rememberNavController()
    val viewModel: WeatherViewModel = viewModel()

    val rawFile = R.raw.weathersunny

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val localDate = LocalDate.parse(date, formatter)
    val locale = viewModel.locale.collectAsState()
    val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale.value).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale.value) else it.toString()
    }

    DayDetails(navController, date, viewModel, dayOfWeek, rawFile, city)
}