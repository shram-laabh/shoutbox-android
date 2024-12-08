package com.example.shoutbox.notificationdb
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notification_table ORDER BY timestamp DESC LIMIT 10")
    fun getTop100Notifications(): Flow<List<NotificationEntity>>
}
