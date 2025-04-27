package com.example.bio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    // "user" or "ai"
    @ColumnInfo(name = "sender") val sender: String,
    // Add the actual content field
    @ColumnInfo(name = "content") val content: String, // Store the message text/prompt here
    // Keep timestamp separate
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    // Add optional field for audio path persistence
    @ColumnInfo(name = "audio_path", defaultValue = "NULL") val audioPath: String? = null
)