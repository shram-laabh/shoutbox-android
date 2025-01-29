package com.shoutboxapp.shoutbox.screenstates

// Define the custom data structure for chat history items
data class ChatMessage(
    val user: String,      // The user who sent the message
    val message: String,   // The content of the message
    val distance: Double // Timestamp of the message (optional)
)

data class ShoutsState(
    var chatHistory: MutableList<ChatMessage> = mutableListOf(),
    var isConnected: Boolean = false,
    var nearbyUsersNum: Int = 0
)
