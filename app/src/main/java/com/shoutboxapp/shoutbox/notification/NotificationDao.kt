package com.shoutboxapp.shoutbox.notification
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notification_table ORDER BY timestamp DESC LIMIT 100")
    fun getTop100Notifications(): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notification_table")
    suspend fun clearTable()

    // Delete entries that are older than the latest 1000
    @Query("""
        DELETE FROM notification_table 
        WHERE id NOT IN (
            SELECT id FROM notification_table 
            ORDER BY timestamp DESC 
            LIMIT 1000
        )
    """)
    suspend fun deleteOldEntries()
}
