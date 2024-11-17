package com.example.shoutbox.notificationdb
import android.content.Context
import androidx.room.Room

class NotificationRepository(context: Context) {
    private val db = Room.databaseBuilder(
        context,
        NotificationDatabase::class.java,
        "notification_database"
    ).build()

    val notificationDao = db.notificationDao()
}
