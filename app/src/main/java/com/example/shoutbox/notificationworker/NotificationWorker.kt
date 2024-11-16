package com.example.shoutbox.notificationworker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.shoutbox.notificationdb.NotificationEntity
import com.example.shoutbox.notificationdb.NotificationRepository

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "No Title"
        val body = inputData.getString("body") ?: "No Body"

        // Get instance of Room database and save data
        val notificationEntity = NotificationEntity(title = title, message = body, timestamp = 25)

        val notificationRepository = NotificationRepository(applicationContext)
        notificationRepository.notificationDao.insert(notificationEntity)

        return Result.success()
    }
}
