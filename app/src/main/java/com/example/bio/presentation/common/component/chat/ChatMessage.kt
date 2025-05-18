package com.example.bio.presentation.common.component.chat

import java.io.Serializable


enum class MessageType {
    TEXT, AUDIO
}

// data class
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val isError: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val audioFilePath: String? = null
) : Serializable