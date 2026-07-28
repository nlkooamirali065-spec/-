package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChatType {
    DIRECT,
    GROUP,
    CHANNEL,
    AI_BOT
}

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val avatarColorHex: String = "#0088CC",
    val chatType: String = ChatType.DIRECT.name,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isVerified: Boolean = false,
    val onlineStatus: String = "آفلاین",
    val phoneNumber: String = "",
    val bio: String = "",
    val memberCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
