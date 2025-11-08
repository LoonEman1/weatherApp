package com.example.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.weatherapp.ui.theme.navigation.NavigationGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val navigationController = rememberNavController()
            Scaffold(modifier = Modifier
                .fillMaxSize(),
                contentColor = MaterialTheme.colorScheme.background
            )
            { innerPadding ->
                Box(modifier = Modifier
                    .padding(innerPadding)
                )
                {
                    NavigationGraph(navController = navigationController)
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WorkManager.getInstance(this.applicationContext).cancelUniqueWork("weather_work_chain")
    }
}
