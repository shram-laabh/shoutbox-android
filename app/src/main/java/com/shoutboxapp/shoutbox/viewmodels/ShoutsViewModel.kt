package com.shoutboxapp.shoutbox.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.shoutboxapp.shoutbox.PermissionManager
import com.shoutboxapp.shoutbox.PermissionManagerSingleton
import com.shoutboxapp.shoutbox.SharedPreferenceStore
import com.shoutboxapp.shoutbox.notification.NameRepository
import com.shoutboxapp.shoutbox.notification.NotificationEntity
import com.shoutboxapp.shoutbox.notification.NotificationRepository
import com.shoutboxapp.shoutbox.screenstates.ChatMessage
import com.shoutboxapp.shoutbox.screenstates.ShoutsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

class ShoutsViewModel(savedStateHandle: SavedStateHandle, private val repository: NotificationRepository, private val nameRepository: NameRepository, application: Application) : AndroidViewModel(application = Application()){
   val data: String = savedStateHandle["dataKey"]?:"Anonym"
   private val prevMessages = SharedPreferenceStore(application)
   val notificationRepository = NotificationRepository(application)
   private val _state = MutableStateFlow(ShoutsState())
   val state: StateFlow<ShoutsState> = _state.asStateFlow()
   private var webSocketClient: WebSocketClient? = null
   private val uri = URI("ws://143.244.133.253:80/ws") // ws://143.244.133.253:8080/ws ws://10.0.2.2:8080/ws

   private val _errorMessage = MutableStateFlow<String?>(null)
   val errorMessage: StateFlow<String?> = _errorMessage

   private val _longitude = MutableStateFlow<Double>(0.0)
   val longitude: StateFlow<Double> = _longitude
   private val _latitude = MutableStateFlow<Double>(0.0)
   val latitude: StateFlow<Double> = _latitude

   private val _fcmToken = MutableLiveData<String>()
   val fcmToken: LiveData<String> get() = _fcmToken
   private val permissionManager = PermissionManagerSingleton.permissionManager

   private val _isLocationAvailable = MutableStateFlow(false) // or MutableLiveData<Boolean>()
   val isLocationAvailable: StateFlow<Boolean> get() = _isLocationAvailable
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

               Log.d("ShoutsViewModel", "Getting Location")
               permissionManager.requestLocationPermissions { lat, lon ->
                  _latitude.value = lat
                  _longitude.value = lon
                  val jsonMessage = """{"type": "token",
               |"longitude": ${_longitude.value},
               |"latitude": ${_latitude.value},
               |"fcmtoken": "${_fcmToken.value}"}""".trimMargin()
                  Log.d("ShoutsViewModel", "JSONTokenMessage = $jsonMessage")
                  sendMessage(jsonMessage)
                  setLocationStatus(true)
               }
            }
         }

         override fun onMessage(message: String?) {
            message?.let { newMessage ->
               // Add message to list and update state flow
               val jsonObject = JSONObject(newMessage)
               Log.d(TAG, "Received Json $newMessage")
               for (key in jsonObject.keys()) {
                  val value = jsonObject.get(key)
                  if (key == "username") {
                  } else if (key == "nearusers"){
                  }
               }
               //  Be curious why we used viewModelScope.launch
               val typeOfResponse = jsonObject.getString("typeofresponse")
               if (typeOfResponse == "chat"){
                     val name = jsonObject.getString("username")
                     val message = jsonObject.getString("message")
                     val distance = jsonObject.getString("distance")
                     val numOfnearbyusers = jsonObject.getInt("nearusers")
                     val timestamp = System.currentTimeMillis()
                     viewModelScope.launch {
                        _state.update { currentState ->
                           val updatedChatHistory = currentState.chatHistory.toMutableList().apply {
                              add(ChatMessage(name, message, distance.toDouble(), timestamp))  // Add new message
                           }
                           currentState.copy(chatHistory = updatedChatHistory, nearbyUsersNum = numOfnearbyusers)
                        }

                        val messageToSave = NotificationEntity(
                           title = name,
                           message = message,
                           timestamp = timestamp,
                           distance = distance.toDouble()
                        )
                        saveMessageToDB(messageToSave)
                        deleteOldMessage()
                        //clearMessages()
                  }
               } else if (typeOfResponse == "token"){
                  val nearusers = jsonObject.getInt("nearusers")
                  Log.d("ShoutboxViewModel", "Nearusers = $nearusers")
                  viewModelScope.launch {
                     _state.update { currentState ->
                        currentState.copy(
                           nearbyUsersNum = nearusers
                        )
                     }
                  }

               } else {
                  Log.d("ShoutboxViewModel", "Message is unrecognizable")
               }
            }
         }

         suspend fun saveMessageToDB(data: NotificationEntity) {
            notificationRepository.notificationDao.insert(data)
         }
         suspend fun deleteOldMessage() {
            notificationRepository.notificationDao.deleteOldEntries()
         }
         suspend fun clearMessages() {
            notificationRepository.notificationDao.clearTable()
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
      startCheckingLocation()
      viewModelScope.launch {
         repository.notificationDao.getTop100Notifications().collect { notificationList ->
            _state.update { currentState ->
               val updatedChatHistory = currentState.chatHistory.toMutableList().apply {
                  for (msg in notificationList.asReversed()){
                     // TODO: Add right distance in State of Chat History
                     add(ChatMessage(msg.title, msg.message, msg.distance, msg.timestamp))  // Add new message
                  }
               }
               currentState.copy(chatHistory = updatedChatHistory)
            }
         }
      }
   }

   private fun startCheckingLocation() {
      viewModelScope.launch {
         while (!_isLocationAvailable.value) {
            if (_fcmToken.value != null){
               getLocation()
            }
            if (_isLocationAvailable.value) {
               break // Stop checking
            }
            delay(1000) // Check every second
         }
      }
   }

   private fun getLocation() {
      // Perform your action here
      Log.d("ShoutsViewModel", "Getting Location")
         permissionManager.requestLocationPermissions { lat, lon ->
            _latitude.value = lat
            _longitude.value = lon
            val jsonMessage = """{"type": "token",
               |"longitude": ${_longitude.value},
               |"latitude": ${_latitude.value},
               |"fcmtoken": "${_fcmToken.value}"}""".trimMargin()
            Log.d("ShoutsViewModel", "JSONTokenMessageAct = $jsonMessage")
            setLocationStatus(true)
            sendMessage(jsonMessage)
         }
   }

   fun setLocationStatus(value: Boolean) {
      _isLocationAvailable.value = value
   }

   fun setName(name: String) {
      viewModelScope.launch {
         nameRepository.setName(name)
      }
   }
   suspend fun getName(): String? {
      return nameRepository.getName()
   }
   fun clearName() {
      viewModelScope.launch {
         nameRepository.clearName()
      }
   }
   private fun connectWebSocket() {
      if (webSocketClient == null || webSocketClient?.isClosed == true) {
         webSocketClient = createWebSocketClient()
         webSocketClient?.connect()
      }
   }
   fun setStatusAsDisconnected() {
      viewModelScope.launch {
         _state.update { currentState ->
            currentState.copy(isConnected = false)
         }
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
   fun clearError() {
      _errorMessage.value = null
   }
}
