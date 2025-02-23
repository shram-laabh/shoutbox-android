package com.shoutboxapp.shoutbox.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shoutboxapp.shoutbox.PermissionManager
import com.shoutboxapp.shoutbox.PermissionManagerSingleton
import com.shoutboxapp.shoutbox.viewmodels.NameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameScreen(navController: NavController,
                viewModel: NameViewModel) {
    val uiState by viewModel.state.collectAsState()
    var nameOfShouter by remember { mutableStateOf("") }

    val context = LocalContext.current

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val isNameValid: (String) -> Boolean = {
        it.length > 3
    }
    var isButtonEnabled = remember{ mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(24.dp)
    ){
        TextField(
            value = nameOfShouter,
            label={Text("Enter a Name to identify with")},
            onValueChange = {
                nameOfShouter = it
                uiState.name = it
                isButtonEnabled.value = isNameValid(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("NameInputField")
        )
        Button(
            onClick = {
                viewModel.setName("$nameOfShouter")
                navController.navigate("chat/$nameOfShouter"){
                    popUpTo("screen1") { inclusive = true }
                }
            },
            enabled = isButtonEnabled.value,
            modifier = Modifier
                .testTag("ShoutButton")
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Shout")
        }

        //Text("Your location is  ${currentLocation.latitude}/${currentLocation.longitude}")
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    NameScreen(navController = rememberNavController(), viewModel = viewModel())
}
