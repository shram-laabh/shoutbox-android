package com.example.shoutbox.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoutbox.screenstates.ShoutsState
import kotlinx.coroutines.Dispatchers
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
   val data: String = savedStateHandle["dataKey"]?:"Anonym"

   private val _state = MutableStateFlow(ShoutsState())
   val state: StateFlow<ShoutsState> = _state.asStateFlow()
   private var webSocketClient: WebSocketClient? = null
   private val uri = URI("ws://144.126.221.138:8080/ws")

   private val _errorMessage = MutableLiveData<String>()
   val errorMessage: LiveData<String> = _errorMessage

   private fun createWebSocketClient() : WebSocketClient{
      return object : WebSocketClient(uri) {
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
            Log.d(TAG, "WKST Closed")
            reconnectWebSocket()
         }

         override fun onError(ex: Exception?) {
            reconnectWebSocket()
         }
      }
   }

   init {
      // Initialize WebSocket and connect
      connectWebSocket()
   }

   private fun connectWebSocket() {
      if (webSocketClient == null || webSocketClient?.isClosed == true) {
         webSocketClient = createWebSocketClient()
         webSocketClient?.connect()
      }
   }
   fun reconnectWebSocket() {
      viewModelScope.launch(Dispatchers.IO) {
         // Optionally add a delay before reconnecting
         // delay(2000) // Delay of 2 seconds
         connectWebSocket()
      }
   }
   override fun onCleared() {
      webSocketClient?.close()
      super.onCleared()
   }
   fun sendMessage(message: String) {
      try{
         webSocketClient?.send(message)
      }catch (e: Exception){
         _errorMessage.value = "Looks like connection is broken!!. Restart your network."
      }
   }
}