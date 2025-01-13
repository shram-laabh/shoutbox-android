package com.shoutboxapp.shoutbox.notification
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "name_table")
data class NameEntity(
    @PrimaryKey val id: Int = 0, // Single row enforced with fixed ID
    val name: String
)
