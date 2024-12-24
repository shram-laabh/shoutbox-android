package com.example.shoutbox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.shoutbox.notificationdb.NotificationDao
import com.example.shoutbox.notificationdb.NotificationDatabase
import com.example.shoutbox.notificationdb.NotificationEntity
import com.example.shoutbox.notificationdb.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// NotificationForegroundService.kt
class NotificationForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title")
        val body = intent?.getStringExtra("body")

        Log.d("FGSERVICE", "In start Command")
        title?.let {
            body?.let {
                saveNotificationData(NotificationEntity(title = title, message =  body, timestamp = 23))
            }
        }

        startForeground(1, createNotification())
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "notification_channel"
        val channel = NotificationChannel(
            channelId,
            "Foreground Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Saving Notifications")
            .setContentText("Running in the background to save notifications.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

    private fun saveNotificationData(data: NotificationEntity) {
        Log.d("FGSERVICE", "SavingNotificationDat")
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("FGSERVICE", "Inside DB IO Co-routine")
            val notificationRepository = NotificationRepository(applicationContext)
            notificationRepository.notificationDao.insert(data)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
