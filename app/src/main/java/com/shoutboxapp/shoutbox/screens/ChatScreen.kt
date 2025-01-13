package com.shoutboxapp.shoutbox.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.NavController
import com.shoutboxapp.shoutbox.viewmodels.ChatViewModel

private const val SERVER_IP = "10.0.2.2"
private const val SERVER_PORT = 8080
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController,
               viewModel: ChatViewModel
) {
    val uiState by viewModel.state.collectAsState()
    var message by remember { mutableStateOf("") }
    Text(text = "Message Recieved")
    Text(text = uiState.currentMessage)
    Text(text = "Send Message")
    TextField(
        value = message,
        onValueChange = {message = it},
        label = {Text("Enter Message")},
        modifier = Modifier.fillMaxWidth()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ){
        Button(
            onClick = {
                viewModel.sendMessage(message)
                 },
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                "Send",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    LaunchedEffect(Unit){
        viewModel.connectToServer(SERVER_IP, SERVER_PORT)
    }
}
