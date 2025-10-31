package com.example.weatherapp.data.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder

import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.weatherapp.data.model.GeoData
import com.google.android.gms.location.LocationServices

class GeoManager(private val context : Context) {

    fun updateLastKnownLocation(activity: Activity, onLocationUpdated : (GeoData?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val locale = context.resources.configuration.locales[0]

        if (!hasFineLocationPermission && !hasCoarseLocationPermission) {
            Log.d("GeoManager", "Location permission not granted")
            Toast.makeText(context, "Нет разрешения на получение местоположения", Toast.LENGTH_SHORT).show()
            onLocationUpdated(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("Проверка", "ПРОВЭЭЭРКА")
                    val geocoder = Geocoder(context, locale)
                    val adresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val city = if (adresses != null && adresses.isNotEmpty()) adresses[0].locality else "unknown"
                    val geoData = GeoData(location.latitude, location.longitude, city)
                    onLocationUpdated(geoData)
                }
                else {
                    onLocationUpdated(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("GeoManager", "Failed to get location", e)
                onLocationUpdated(null)
            }
    }
}