package com.shoutboxapp.shoutbox.notification

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class NotificationDbApp : Application() {
    lateinit var database: NotificationDatabase
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Perform necessary schema changes here
            // Example: database.execSQL("ALTER TABLE table_name ADD COLUMN new_column INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE notification_table ADD COLUMN distance REAL NOT NULL DEFAULT 0.0")
        }
    }
    lateinit var nameDatabase: NameDatabase
    lateinit var repository: NameRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            NotificationDatabase::class.java,
            "app_database"
        ).addMigrations(MIGRATION_1_2).build()
        nameDatabase = Room.databaseBuilder(
            applicationContext,
            NameDatabase::class.java,
            "name_database"
        ).build()

        repository = NameRepository(nameDatabase.nameDao())
    }
}