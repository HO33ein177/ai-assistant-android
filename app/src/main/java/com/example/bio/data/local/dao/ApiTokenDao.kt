package com.example.bio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bio.data.local.entity.ApiToken

@Dao
interface ApiTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(apiToken: ApiToken)

    @Query("SELECT * FROM api_tokens WHERE user_id = :userId")
    suspend fun getApiToken(userId: Int): ApiToken?
}