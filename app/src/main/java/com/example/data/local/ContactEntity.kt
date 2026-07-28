package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val bio: String = "",
    val avatarColorHex: String = "#0E8388",
    val isOnline: Boolean = false,
    val lastSeen: String = "چند لحظه پیش"
)
