package com.shoutboxapp.shoutbox.notification

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NameEntity::class], version = 1)
abstract class NameDatabase : RoomDatabase() {
    abstract fun nameDao(): NameDao
}
