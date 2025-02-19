package com.shoutboxapp.shoutbox.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoutboxapp.shoutbox.repositories.ChatServerRepository
import com.shoutboxapp.shoutbox.screenstates.ChatState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel"

class ChatViewModel(private val repository: ChatServerRepository) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun connectToServer(host: String, port: Int) {
        viewModelScope.launch {
            val connected = repository.connectToServer(host, port)
            Log.d(TAG, "Result of connection = , $connected")
           /* TODO: Why update doesn't happen of the state
                _state.update { currentState ->
                currentState.copy(isConnected = connected)
            }*/
            startListening()
        }
    }

    fun sendMessage(message: String) {
        Log.d(TAG, "Sending message = $message")
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessage(message)
            Log.d(TAG, "Message Sent $message")
        }
    }

    private fun startListening() {
        viewModelScope.launch {
            while (true) {
                val response = repository.receiveMessage()
                response?.let {
                    _state.update { currentState ->
                        currentState.copy(currentMessage = it)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.closeConnection()
    }
}