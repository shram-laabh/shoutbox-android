package com.example.shoutbox.notification
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class NotificationRepository(context: Context) {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Perform necessary schema changes here
            // Example: database.execSQL("ALTER TABLE table_name ADD COLUMN new_column INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE notification_table ADD COLUMN distance REAL NOT NULL DEFAULT 0.0")
        }
    }
    private val db = Room.databaseBuilder(
        context,
        NotificationDatabase::class.java,
        "notification_database"
    ).addMigrations(MIGRATION_1_2).build()

    val notificationDao = db.notificationDao()
}
