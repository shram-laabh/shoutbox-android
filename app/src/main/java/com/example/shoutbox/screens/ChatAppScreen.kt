package com.example.shoutbox.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shoutbox.PermissionManager
import com.example.shoutbox.viewmodels.ShoutsViewModel
import com.google.android.gms.maps.model.LatLng


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppScreen(
    navController: NavController,
    viewModel: ShoutsViewModel,
    nameString: String?,
) {
    val uiState by viewModel.state.collectAsState()
    var message by remember { mutableStateOf(TextFieldValue("")) }
    //var chatHistory by remember { mutableStateOf(listOf<String>()) }

    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) } // Initialize directly

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
        LaunchedEffect(Unit) {
            // Request location and get the latitude and longitude via callback
            permissionManager.requestLocationPermissions { lat, lon ->
                latitude = lat
                longitude = lon
                val jsonMessage = """{"type": "token",
               |"longitude": ${longitude},
               |"latitude": ${latitude},
               |"fcmtoken": "${viewModel.fcmToken.value}"}""".trimMargin()
                Log.d("ChatAppScreen", "Token Message = $jsonMessage")
                viewModel.sendMessage(jsonMessage)
            }
        }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            uiState.chatHistory.forEach { message ->
                Text(text = message)
            }
        }
        TextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Text("Your location is  ${latitude}/${longitude}")
        Button(
            onClick = {
                val jsonMessage = """{"type" : "chat",
                    |"username": "$nameString", 
                    |"longitude":${longitude},
                    |"latitude":${latitude},
                    |"message": "${message.text}"}""".trimMargin()
                viewModel.sendMessage(jsonMessage)
                message = TextFieldValue("")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }
    }
}
