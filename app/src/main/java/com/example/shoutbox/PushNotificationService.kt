package com.example.shoutbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.shoutbox.notificationdb.NotificationDbApp
import com.example.shoutbox.notificationdb.NotificationEntity
import com.example.shoutbox.notificationworker.NotificationWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle FCM notification
        Log.d("FCM_svc", "Got Message")
        val notification = remoteMessage.notification
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data

            // Save data payload in Room Database or SharedPreferences
            startFGService(data["title"], data["body"])
        }
        if (notification != null) {
            Log.d("FCM_svc", "Message Notification Body: ${notification.title} ${notification.body}")
            sendNotification(notification.title, notification.body)
        }
    }
    private fun startFGService(title: String?, body: String?){
        val intent = Intent(this, NotificationForegroundService::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
        }

        ContextCompat.startForegroundService(applicationContext, intent)
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle the new FCM token, typically by sending it to the server
        Log.d("FCM_svc", "On New Token")
    }
    private fun sendNotification(title: String?, message: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        Log.d("FCM_svc", "Send Notification")
        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

