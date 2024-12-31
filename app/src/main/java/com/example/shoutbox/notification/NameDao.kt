package com.example.shoutbox.notification

import androidx.room.*

@Dao
interface NameDao {
    @Query("SELECT name FROM name_table LIMIT 1")
    suspend fun getName(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertName(nameEntity: NameEntity)

    @Query("DELETE FROM name_table")
    suspend fun clearName()
}
