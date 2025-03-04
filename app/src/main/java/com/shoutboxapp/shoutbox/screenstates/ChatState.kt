package com.shoutboxapp.shoutbox.screenstates

data class ChatState(
    val listOfMessages: List<String> = listOf(),
    val isConnected: Boolean = false,
    val currentMessage: String = "",
)
