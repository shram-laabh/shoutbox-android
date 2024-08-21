package com.example.shoutbox.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoutbox.screenstates.ShoutsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

private const val TAG = "ShoutsViewModel"
class ShoutsViewModel(savedStateHandle: SavedStateHandle) : ViewModel(){
   val data: String = savedStateHandle["dataKey"]?:""
   private val _state = MutableStateFlow(ShoutsState())
   val state: StateFlow<ShoutsState> = _state.asStateFlow()
   private val uri = URI("ws://144.126.221.138:8080/ws")

   private val webSocketClient: WebSocketClient = object : WebSocketClient(uri) {
      override fun onOpen(handshakedata: ServerHandshake?) {
         // WebSocket connection opened
         viewModelScope.launch {
            _state.update { currentState ->
               currentState.copy(isConnected = true)
            }
         }
      }

      override fun onMessage(message: String?) {
         message?.let { newMessage ->
            // Add message to list and update state flow
            val jsonObject = JSONObject(newMessage)
            val name = jsonObject.getString("username")
            val message = jsonObject.getString("message")
            //  Be curious why we used viewModelScope.launch
            viewModelScope.launch {
               _state.update { currentState ->
                  val updatedChatHistory = currentState.chatHistory.toMutableList().apply {
                     add("$name: $message")  // Add new message
                  }
                  currentState.copy(chatHistory = updatedChatHistory)
               }
            }
         }
      }

      override fun onClose(code: Int, reason: String?, remote: Boolean) {
         // WebSocket connection closed
      }

      override fun onError(ex: Exception?) {
         viewModelScope.launch {
            _state.update { currentState ->
               currentState.copy(isConnected = false)
            }
         }
         ex?.printStackTrace()
      }
   }.apply { connect() }

   init {
      // Initialize WebSocket and connect
   }

   override fun onCleared() {
      super.onCleared()
      // Clean up resources
      webSocketClient.close()
   }
   fun sendMessage(message: String) {
      webSocketClient.send(message)
   }
}