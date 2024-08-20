package com.example.shoutbox.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

const val TAG = "ChatScreen"

@Serializable
data class MessageJson(val username: String, val message: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppScreen() {
    var message by remember { mutableStateOf(TextFieldValue("")) }
    var chatHistory by remember { mutableStateOf(listOf<String>()) }

    val wsClient = remember {
            createWebSocketClient(chatHistory, onNewMessage = {
                //val msgJson = Json.decodeFromString<MessageJson>(it)
                //Log.d(TAG, "Name = ${msgJson.username}, ${msgJson.message}")
                chatHistory += it // msgJson.message
            })
    }

    Log.d(TAG, "wsClient = , ${wsClient.isClosed}")
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            chatHistory.forEach { message ->
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
            onClick = {
                val jsonMessage = """{"username": "user1", "message": "${message.text}"}"""
                wsClient.send(jsonMessage)
                message = TextFieldValue("")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }
    }
}


fun createWebSocketClient(chatHistory: List<String>, onNewMessage: (String) -> Unit): WebSocketClient {
    val uri = URI("ws://144.126.221.138:8080/ws")

    return object : WebSocketClient(uri) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            // WebSocket connection opened
        }

        override fun onMessage(message: String?) {
            message?.let { onNewMessage(it) }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            // WebSocket connection closed
        }

        override fun onError(ex: Exception?) {
            ex?.printStackTrace()
        }
    }.apply { connect() }
}