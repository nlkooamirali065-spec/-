package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    FILE,
    POLL
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val senderName: String,
    val isFromUser: Boolean,
    val content: String,
    val messageType: String = MessageType.TEXT.name,
    val isRead: Boolean = true,
    val replyToText: String? = null,
    val voiceDurationSeconds: Int = 0,
    val fileSizeMb: Double = 0.0,
    val formattedTime: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
