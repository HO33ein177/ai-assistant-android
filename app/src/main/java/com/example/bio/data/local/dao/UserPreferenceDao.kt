package com.example.bio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bio.data.local.entity.UserPreferences

@Dao
interface UserPreferenceDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userPreferences: UserPreferences)

    @Update
    suspend fun update(userPreferences: UserPreferences)

    @Query("SELECT * FROM user_preferences WHERE user_id = :userId")
    suspend fun getUserPreferences(userId: Int): UserPreferences?

}