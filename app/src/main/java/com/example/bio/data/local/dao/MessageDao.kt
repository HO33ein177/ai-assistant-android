package com.example.bio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bio.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages WHERE user_id = :userId AND conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(userId: Int, conversationId: String): Flow<List<Message>>

    @Query("SELECT DISTINCT conversation_id FROM messages WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getAllConversationIds(userId: Int): Flow<List<String>>
}