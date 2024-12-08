package com.example.shoutbox.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        "Hello!",
        "How are you?",
        "Channel",
        ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby People") },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Middle Scrollable Window
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    items(messages) { msg ->
                        Text(
                            text = msg,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Enter a Shout") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (message.isNotBlank()) {
                                messages.add(message)
                                message = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (message.isNotBlank()) {
                            messages.add(message)
                            message = ""
                        }
                    }) {
                        Text("Shout")
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}
