package com.example.shoutbox

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class SharedPreferenceStore(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val MESSAGES_KEY = "messages_key"
        private const val MAX_MESSAGES = 100  // Limit of 100 messages
    }

    // Save a notification message
    fun saveNotification(message: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing messages
        val messages = getNotifications().toMutableList()

        // Add new message to the list
        messages.add(message)

        // Limit the list to MAX_MESSAGES (100 messages)
        if (messages.size > MAX_MESSAGES) {
            messages.removeAt(0)  // Remove the oldest message (first element)
        }

        // Save the updated list as a JSON string
        val jsonArray = JSONArray(messages)
        editor.putString(MESSAGES_KEY, jsonArray.toString())
        editor.apply()  // Save changes
    }

    // Retrieve notification messages
    fun getNotifications(): List<String> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonMessages = sharedPreferences.getString(MESSAGES_KEY, null) ?: return emptyList()

        val messages = mutableListOf<String>()
        val jsonArray = JSONArray(jsonMessages)

        for (i in 0 until jsonArray.length()) {
            messages.add(jsonArray.getString(i))
        }

        return messages
    }
}
