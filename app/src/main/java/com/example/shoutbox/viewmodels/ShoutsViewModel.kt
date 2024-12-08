package com.example.shoutbox.viewmodels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoutbox.SharedPreferenceStore
import com.example.shoutbox.notificationdb.NotificationEntity
import com.example.shoutbox.notificationdb.NotificationRepository
import com.example.shoutbox.screenstates.ChatMessage
import com.example.shoutbox.screenstates.ShoutsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Flow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

private const val TAG = "ShoutsViewModel"

class ShoutsViewModel(savedStateHandle: SavedStateHandle, private val repository: NotificationRepository, application: Application) : AndroidViewModel(application = Application()){
   val data: String = savedStateHandle["dataKey"]?:"Anonym"
   private val prevMessages = SharedPreferenceStore(application)
   private val _state = MutableStateFlow(ShoutsState())
   val state: StateFlow<ShoutsState> = _state.asStateFlow()
   private var webSocketClient: WebSocketClient? = null
   private val uri = URI("ws://10.0.2.2:8080/ws") //  ws://144.126.221.138:8080/ws

   private val _errorMessage = MutableLiveData<String>()
   val errorMessage: LiveData<String> = _errorMessage

   private val _fcmToken = MutableLiveData<String>()
   val fcmToken: LiveData<String> get() = _fcmToken

   // Function to set the FCM token
   fun setFcmToken(token: String) {
      _fcmToken.value = token
   }
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
               Log.d(TAG, "Received Json $newMessage")
               val name = jsonObject.getString("username")
               val message = jsonObject.getString("message")
               val distance = jsonObject.getString("distance")
               //  Be curious why we used viewModelScope.launch
               viewModelScope.launch {
                  _state.update { currentState ->
                     val updatedChatHistory = currentState.chatHistory.toMutableList().apply {
                        add(ChatMessage(name, message, distance.toDouble()))  // Add new message
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
            Log.d(TAG, "SocketEx: Got error on socket $ex")
            reconnectWebSocket()
         }
      }
   }
   init {
      // Initialize WebSocket and connect
      connectWebSocket()

      viewModelScope.launch {
         repository.notificationDao.getTop100Notifications().collect { notificationList ->
            _state.update { currentState ->
               val updatedChatHistory = currentState.chatHistory.toMutableList().apply {
                  for (msg in notificationList){
                     add(ChatMessage("User", msg.message, 2343.34))  // Add new message
                  }
               }
               currentState.copy(chatHistory = updatedChatHistory)
            }
         }
      }
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
