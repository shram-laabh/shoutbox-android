package com.shoutboxapp.shoutbox

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

data class LocationData(val latitude: Double, val longitude: Double)
class PermissionManager(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private lateinit var locationLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var notificationLauncher: ActivityResultLauncher<String>

    private val _locationData = MutableLiveData<LocationData>()
    val locationData: LiveData<LocationData> = _locationData
    fun setLocationLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        this.locationLauncher = launcher
    }

    fun requestLocationPermissions(onLocationReceived: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
                locationLauncher.launch(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
        } else {
            getCurrentLocation(onLocationReceived)
        }
    }

    @SuppressLint("MissingPermission") // Suppress the warning since permissions are handled above
    private fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        Log.d("ShoutsViewModel", "Attempting to fetch location...")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    //Toast.makeText(context, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_LONG).show()
                    onLocationReceived(latitude, longitude)
                    Log.d("ShoutsViewModel", "Received location: Lat = ${location.latitude}, Lon = ${location.longitude}")
                } else {
                    Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                    Log.d("ShoutsViewModel", "Last Location is null, requesting a new one...")
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.lastLocation?.let { newLocation ->
                                Log.d("ShoutsViewModel", "New Location: Lat = ${newLocation.latitude}, Lon = ${newLocation.longitude}")
                                onLocationReceived(newLocation.latitude, newLocation.longitude)
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Error getting location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
