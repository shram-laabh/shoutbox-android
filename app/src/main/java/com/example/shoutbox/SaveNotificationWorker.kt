package com.example.shoutbox

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.shoutbox.notificationdb.NotificationDbApp
import com.example.shoutbox.notificationdb.NotificationEntity

class SaveNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title")
        val body = inputData.getString("body")

        if (title != null && body != null) {
            val notification = NotificationEntity(
                title = title,
                message = body,
                timestamp = System.currentTimeMillis()
            )

            val database = (applicationContext as NotificationDbApp).database
            database.notificationDao().insert(notification)
        }

        return Result.success()
    }
}
