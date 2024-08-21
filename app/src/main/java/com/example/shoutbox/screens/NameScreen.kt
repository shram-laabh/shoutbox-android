package com.example.shoutbox.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.shoutbox.viewmodels.NameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameScreen(navController: NavController,
                viewModel: NameViewModel) {
    val uiState by viewModel.state.collectAsState()
    var nameOfShouter by remember { mutableStateOf("") }

    Column(){
        TextField(
            value = nameOfShouter,
            onValueChange = {
                nameOfShouter = it
                uiState.name = it.toString()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = {
                navController.navigate("chat/${uiState.name}")
            },
        ) {
            Text("Shout")
        }
    }
}