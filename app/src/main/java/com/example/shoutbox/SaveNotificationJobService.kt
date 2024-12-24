package com.example.shoutbox

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.example.shoutbox.notificationdb.NotificationDatabase
import com.example.shoutbox.notificationdb.NotificationEntity
import com.example.shoutbox.notificationdb.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SaveNotificationJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        val title = params?.extras?.getString("title")
        val body = params?.extras?.getString("body")

        // Perform your task in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            // Save data to RoomDB
            var notifEntity: NotificationEntity? = null

            title?.let {
                body?.let {
                    notifEntity = NotificationEntity(title = title, message =  body, timestamp = 23)
                }
            }
            val notificationRepository = NotificationRepository(applicationContext)
            notifEntity?.let { notificationRepository.notificationDao.insert(it) }
            // Notify JobScheduler that the job is finished
            jobFinished(params, false)
        }

        // Return true as the job runs asynchronously
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Handle job cancellation if needed
        return false
    }
}
