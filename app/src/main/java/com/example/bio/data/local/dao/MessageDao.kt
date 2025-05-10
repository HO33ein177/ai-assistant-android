package com.example.bio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bio.data.local.entity.Message
import com.example.bio.presentation.common.component.chat.ConversationSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages WHERE user_id = :userId AND conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(userId: Int, conversationId: String): Flow<List<Message>>

    // Kept the old query for distinct IDs if it's used elsewhere,
    // but the new one is preferred for the history page.
    @Query("SELECT DISTINCT conversation_id FROM messages WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getAllConversationIds(userId: Int): Flow<List<String>>

    // New query to get conversation summaries
    // It selects the conversation_id, the content of the first message (min timestamp),
    // and the timestamp of the last message (max timestamp) for each conversation.
    @Query("""
        SELECT 
            m.conversation_id as conversationId,
            MAX(m.timestamp) as lastMessageTimestamp,
            (SELECT content FROM messages 
             WHERE conversation_id = m.conversation_id AND user_id = :userId 
             ORDER BY timestamp ASC LIMIT 1) as firstMessageContent
        FROM messages m
        WHERE m.user_id = :userId
        GROUP BY m.conversation_id
        ORDER BY lastMessageTimestamp DESC
    """)
    fun getConversationSummaries(userId: Int): Flow<List<ConversationSummary>>
}