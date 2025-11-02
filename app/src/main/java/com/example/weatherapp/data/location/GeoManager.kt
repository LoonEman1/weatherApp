package com.example.weatherapp.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder

import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.weatherapp.data.model.GeoData
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GeoManager() {

    suspend fun updateLastKnownLocation(context : Context) : GeoData? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val locale = context.resources.configuration.locales[0]

        if(!hasCoarseLocationPermission && !hasFineLocationPermission) {
            Log.d("permissions", "No permissions")
            return null
        }

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(context, locale)
                        val adresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val city =
                            if (adresses != null && adresses.isNotEmpty()) adresses[0].locality else "unknown"
                        val geoData = GeoData(location.latitude, location.longitude, city)
                        Log.d("Geodata", geoData.toString())
                        cont.resume((GeoData(location.latitude, location.longitude, city)))
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GeoManager", "Failed to get location", e)
                    cont.resume(null)
                }
        }
    }
}