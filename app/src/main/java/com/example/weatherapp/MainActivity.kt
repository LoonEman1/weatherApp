package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.example.weatherapp.data.workers.WorkManagerController
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

        Log.d("MainActivity", "onDestroy: Checking WorkManager status before cancellation")
        val workManager = WorkManager.getInstance(this.applicationContext)
        val workInfos = workManager.getWorkInfosForUniqueWork("weather_work_chain").get()
        if (workInfos.isNotEmpty()) {
            workInfos.forEach { info ->
                Log.d("WorkStatus", "Before cancellation: Work ${info.id} - status: ${info.state}")
            }
        } else {
            Log.d("WorkStatus", "Before cancellation: No active workers")
        }

        Log.d("MainActivity", "Cancelling weather_work_chain...")
        workManager.cancelUniqueWork("weather_work_chain")

        Log.d("MainActivity", "After cancelUniqueWork: Checking status")
        val workInfosAfter = workManager.getWorkInfosForUniqueWork("weather_work_chain").get()
        if (workInfosAfter.isNotEmpty()) {
            workInfosAfter.forEach { info ->
                Log.d("WorkStatus", "After cancellation: Work ${info.id} - status: ${info.state}")
            }
        } else {
            Log.d("WorkStatus", "After cancellation: All workers cancelled successfully")
        }
    }
}
