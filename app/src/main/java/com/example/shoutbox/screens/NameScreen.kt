package com.example.shoutbox.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.shoutbox.PermissionManager
import com.example.shoutbox.viewmodels.NameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameScreen(navController: NavController,
                viewModel: NameViewModel) {
    val uiState by viewModel.state.collectAsState()
    var nameOfShouter by remember { mutableStateOf("") }

    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    // Notification permission launcher (For Android 13 and above)

    // Location permission launcher
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
            // Now get the current location
            permissionManager.requestLocationPermissions { lat, lon ->
                latitude = lat
                longitude = lon
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    permissionManager.setLocationLauncher(locationLauncher)

    Column{
        TextField(
            value = nameOfShouter,
            onValueChange = {
                nameOfShouter = it
                uiState.name = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("NameInputField")
        )
        Button(
            onClick = {
                navController.navigate("chat/${uiState.name}")
            },
            modifier = Modifier
                .testTag("ShoutButton")
        ) {
            Text("Shout")
        }

        //Text("Your location is  ${currentLocation.latitude}/${currentLocation.longitude}")
    }
    LaunchedEffect(Unit){
        //permissionManager.requestNotificationPermission()

        permissionManager.requestLocationPermissions { lat, lon ->
            latitude = lat
            longitude = lon
        }
    }
}