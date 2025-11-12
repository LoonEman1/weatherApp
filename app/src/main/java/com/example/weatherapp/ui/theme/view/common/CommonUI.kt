package com.example.weatherapp.ui.theme.view.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun WeatherText(text : String, fontSize: TextUnit? = null) {
    Text(
        text = text,
        fontSize = fontSize ?: 20.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif,
        color = Color.White,
        fontWeight = FontWeight.Black,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.8f),
                offset = Offset(2f, 2f),
                blurRadius = 6f
            )
        )
    )
}


fun formatTemperature(temp: Double?): String {
    if (temp == null) {
        return "—°C"
    }
    val sign = if (temp > 0) "+" else ""
    return "${sign + temp}°C"
}


@Composable
@Preview
fun PreviewWeatherText() {
    WeatherText("test")
}