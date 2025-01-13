package com.shoutboxapp.shoutbox.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.shoutboxapp.shoutbox.SharedPreferenceStore
import com.shoutboxapp.shoutbox.notification.NameRepository
import com.shoutboxapp.shoutbox.notification.NotificationEntity
import com.shoutboxapp.shoutbox.notification.NotificationRepository
import com.shoutboxapp.shoutbox.screenstates.ChatMessage
import com.shoutboxapp.shoutbox.screenstates.ShoutsState
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

class ShoutsViewModel(savedStateHandle: SavedStateHandle, private val repository: NotificationRepository, private val nameRepository: NameRepository, application: Application) : AndroidViewModel(application = Application()){
   val data: String = savedStateHandle["dataKey"]?:"Anonym"
   private val prevMessages = SharedPreferenceStore(application)
   val notificationRepository = NotificationRepository(application)
   private val _state = MutableStateFlow(ShoutsState())
   val state: StateFlow<ShoutsState> = _state.asStateFlow()
   private var webSocketClient: WebSocketClient? = null
   private val uri = URI("ws://143.244.133.253:8080/ws") //ws://144.126.221.138:8080/ws ws://10.0.2.2:8080/ws

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

                  val messageToSave = NotificationEntity(
                     title = name,
                     message = message,
                     timestamp = System.currentTimeMillis(),
                     distance = distance.toDouble()
                  )
                  saveMessageToDB(messageToSave)
                  //clearMessages()
               }
            }
         }

         suspend fun saveMessageToDB(data: NotificationEntity) {
            notificationRepository.notificationDao.insert(data)
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

      viewModelScope.launch {
         repository.notificationDao.getTop100Notifications().collect { notificationList ->
            _state.update { currentState ->
               val updatedChatHistory = currentState.chatHistory.toMutableList().apply {
                  for (msg in notificationList.asReversed()){
                     // TODO: Add right distance in State of Chat History
                     add(ChatMessage(msg.title, msg.message, msg.distance))  // Add new message
                  }
               }
               currentState.copy(chatHistory = updatedChatHistory)
            }
         }
      }
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
