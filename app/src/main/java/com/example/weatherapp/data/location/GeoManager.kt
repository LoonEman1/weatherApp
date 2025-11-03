package com.example.weatherapp.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper

import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.weatherapp.data.model.GeoData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GeoManager() {


    suspend fun updateLastKnownLocation(context: Context): GeoData? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val locale = context.resources.configuration.locales[0]

        if (!hasCoarseLocationPermission && !hasFineLocationPermission) {
            Log.d("permissions", "No permissions")
            return null
        }

        return suspendCancellableCoroutine { cont ->

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val geoData = geocodeLocation(location, context, locale)
                        Log.d("Geodata", geoData.toString())
                        cont.resume(geoData)
                    }
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        Log.d("LocationRequest", "try to get location")
                        val locationRequest = LocationRequest.Builder(1000)
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setIntervalMillis(0L)
                            .setMinUpdateIntervalMillis(0L)
                            .setMaxUpdates(1)
                            .build()

                        val locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                val loc = locationResult.lastLocation
                                if (loc != null) {
                                    val geoData = geocodeLocation(loc, context, locale)
                                    Log.d("Geodata", geoData.toString())
                                    cont.resume(geoData)
                                } else {
                                    Log.e("GeoManager", "Failed to get location")
                                    cont.resume(null)
                                }
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }

                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("GeoManager", "Failed to get location", e)
                cont.resume(null)
            }

            cont.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
            }
        }
    }

    private fun geocodeLocation(
        location: Location,
        context: Context,
        locale: java.util.Locale
    ): GeoData {
        val geocoder = Geocoder(context, locale)
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val city = if (addresses != null && addresses.isNotEmpty()) addresses[0].locality else "unknown"
        return GeoData(location.latitude, location.longitude, city)
    }
}