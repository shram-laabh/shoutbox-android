package com.example.shoutbox.screenstates

data class ShoutsState(
    var chatHistory: MutableList<String> = mutableListOf(),
    var isConnected: Boolean = false
)
