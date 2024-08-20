package com.example.shoutbox.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.shoutbox.viewmodels.NameViewModel

@Composable
fun NameScreen(navController: NavController,
                viewModel: NameViewModel) {
    val uiState by viewModel.state.collectAsState()
    Text(text = uiState.name)
    Button(onClick = {
    /*TODO*/
        navController.navigate("chat")
    }) {
        Text("GoChat")
    }
}