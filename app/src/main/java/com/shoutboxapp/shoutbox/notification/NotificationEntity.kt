package com.shoutboxapp.shoutbox.notification
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val distance: Double = 0.0,
    val timestamp: Long,
)
