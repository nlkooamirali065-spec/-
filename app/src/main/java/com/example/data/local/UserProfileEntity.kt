package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "کاربر نیکا",
    val username: String = "nika_user",
    val bio: String = "در حال استفاده از پیام‌رسان نیکا ✨",
    val phoneNumber: String = "+98 912 345 6789",
    val avatarColorHex: String = "#0E8388",
    val isDarkTheme: Boolean = true,
    val themeColorHex: String = "#0E8388",
    val notificationsEnabled: Boolean = true,
    val isPasscodeEnabled: Boolean = false
)
