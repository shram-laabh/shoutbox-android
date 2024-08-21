package com.example.shoutbox.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shoutbox.viewmodels.ShoutsViewModel

const val TAG = "ChatScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppScreen(
    navController: NavController,
    viewModel: ShoutsViewModel,
    string: String?
) {
    val uiState by viewModel.state.collectAsState()
    var message by remember { mutableStateOf(TextFieldValue("")) }
    //var chatHistory by remember { mutableStateOf(listOf<String>()) }

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
        Button(
            enabled = uiState.isConnected,
            onClick = {
                val jsonMessage = """{"username": "${viewModel.data}", "message": "${message.text}"}"""
                viewModel.sendMessage(jsonMessage)
                message = TextFieldValue("")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }
    }
}
