package com.example.weatherapp.ui.theme.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.ui.theme.navigation.Screen

@Composable
fun WelcomeScreen(navController : NavHostController) {
    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
        {
            Text(
                text = "Приложение прогноза погоды!",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 20.dp),
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier.padding(top = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                FeatureRow(
                    icon = "✓",
                    text = "Прогноз погоды для любого города",
                    padding = 20.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = "✓",
                    text = "Сохранение данных локально",
                    padding = 20.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = "✓",
                    text = "Автоматическое обновление данных в фоне",
                    padding = 20.dp
                )
            }
            Spacer(modifier = Modifier
                .weight(1f))
            Text(
                text = "Для продолжения нажмите на кнопку",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp)
            )
            Button(
                onClick = {
                    navController.navigate(route = Screen.WeatherScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .align(Alignment.CenterHorizontally)
                    .wrapContentHeight(align = Alignment.Bottom),
            ) {
                Text("Следующий экран")
            }
        }
    }
}

@Composable
@Preview
fun ShowWelcomeScreen() {
    val navController = rememberNavController()
    WelcomeScreen(navController)
}

@Composable
fun FeatureRow(icon: String, text: String, padding: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp, start = padding)
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}