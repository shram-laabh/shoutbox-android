package com.example.shoutbox.notificationdb

import android.app.Application
import androidx.room.Room

class NotificationDbApp : Application() {
    lateinit var database: NotificationDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            NotificationDatabase::class.java,
            "app_database"
        ).build()
    }
}