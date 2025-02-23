package com.shoutboxapp.shoutbox.screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.shoutboxapp.shoutbox.PermissionManager
import com.shoutboxapp.shoutbox.PermissionManagerSingleton
import com.shoutboxapp.shoutbox.viewmodels.NameViewModel

@Composable
fun LaunchScreen(navController: NavController, viewModel: NameViewModel, nameOfUser: String?) {
    val nameExists by viewModel.nameExists.observeAsState()

    LaunchedEffect(nameExists) {
        Log.d("DEBUG", "nameExists value: $nameExists")
        // Run your location launcher logic
        // Navigate to the appropriate screen
        when (nameExists) {
            true -> navController.navigate("chat/${nameOfUser}") {
                popUpTo("launch") { inclusive = true }
            }
            false -> navController.navigate("name") {
                popUpTo("launch") { inclusive = true }
            }
            null -> { /* Do nothing yet, wait for a value */ }
        }
    }

    // Optionally, show a loading UI while the logic runs
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
