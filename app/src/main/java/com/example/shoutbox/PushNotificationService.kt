package com.example.shoutbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.shoutbox.notificationdb.NotificationDbApp
import com.example.shoutbox.notificationdb.NotificationEntity
import com.example.shoutbox.notificationdb.NotificationRepository
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
            // startFGService(data["title"], data["body"])
            // SaveNotificationToRoom(data["title"], data["body"])
            // saveNotificationUsingWorker()
            //scheduleJob(data["title"], data["body"])
            saveDataToRoom(data["title"], data["body"])
        }
        if (notification != null) {
            Log.d("FCM_svc", "Message Notification Body: ${notification.title} ${notification.body}")
            sendNotification(notification.title, notification.body)
        }
    }

    private fun saveNotificationUsingWorker(title: String?, body: String?) {
        val workData = Data.Builder()
            .putString("title", title)
            .putString("body", body)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    private fun scheduleJob(title: String?, body: String?) {
        val componentName = ComponentName(this, SaveNotificationJobService::class.java)
        val jobInfo = JobInfo.Builder(1234, componentName) // Unique job ID
            .setExtras(PersistableBundle().apply {
                putString("title", title)
                putString("body", body)
            })
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Set network requirements
            .setPersisted(true) // Persist across reboots
            .build()

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }
    private fun saveDataToRoom(title: String?, body: String?) {
        Thread {
            // Save data in Room database (or SharedPreferences)
            var notifEntity: NotificationEntity? = null
            title?.let {
                body?.let {
                    notifEntity = NotificationEntity(title = title, message =  body, timestamp = System.currentTimeMillis()/1000)
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("FGSERVICE", "Inside DB IO Co-routine")
                if (notifEntity != null){
                    val notificationRepository = NotificationRepository(applicationContext)
                    notificationRepository.notificationDao.insert(notifEntity!!)
                }
            }
        }.start()
    }
    private fun SaveNotificationToRoom(title: String?, body: String?){
        var notifEntity: NotificationEntity? = null
        title?.let {
            body?.let {
                notifEntity = NotificationEntity(title = title, message =  body, timestamp = 23)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("FGSERVICE", "Inside DB IO Co-routine")
            if (notifEntity != null){
                val notificationRepository = NotificationRepository(applicationContext)
                notificationRepository.notificationDao.insert(notifEntity!!)
            }
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

