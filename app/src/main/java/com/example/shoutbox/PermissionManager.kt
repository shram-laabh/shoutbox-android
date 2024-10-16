package com.example.shoutbox

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

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

    fun setNotificationLauncher(launcher: ActivityResultLauncher<String>) {
        this.notificationLauncher = launcher
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

            Toast.makeText(context, "Location permission already granted", Toast.LENGTH_SHORT).show()
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            getCurrentLocation(onLocationReceived)
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
             //   Toast.makeText(context, "Notification permission already granted", Toast.LENGTH_SHORT).show()
                Log.d("Creation", "Seeing notification")
            }
        } else {
            Toast.makeText(context, "Notification permission not needed below Android 13", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission") // Suppress the warning since permissions are handled above
    private fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(context, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_LONG).show()
                    onLocationReceived(latitude, longitude)
                } else {
                    Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Error getting location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
