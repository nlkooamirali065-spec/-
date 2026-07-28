package com.example.data.local

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NikaRepository(private val database: NikaDatabase) {
    val chatDao = database.chatDao()
    val messageDao = database.messageDao()
    val contactDao = database.contactDao()
    val userProfileDao = database.userProfileDao()

    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfileFlow()

    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForChat(chatId)
    }

    fun getChatByIdFlow(chatId: Long): Flow<ChatEntity?> {
        return chatDao.getChatByIdFlow(chatId)
    }

    suspend fun clearUnreadCount(chatId: Long) {
        chatDao.clearUnreadCount(chatId)
    }

    suspend fun sendMessage(
        chatId: Long,
        senderName: String,
        content: String,
        isFromUser: Boolean,
        messageType: String = MessageType.TEXT.name,
        replyToText: String? = null,
        voiceDurationSeconds: Int = 0,
        fileSizeMb: Double = 0.0
    ): Long {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale("fa"))
        val formattedTime = timeFormat.format(Date(now))

        val message = MessageEntity(
            chatId = chatId,
            senderName = senderName,
            isFromUser = isFromUser,
            content = content,
            messageType = messageType,
            isRead = true,
            replyToText = replyToText,
            voiceDurationSeconds = voiceDurationSeconds,
            fileSizeMb = fileSizeMb,
            formattedTime = formattedTime,
            timestamp = now
        )

        val msgId = messageDao.insertMessage(message)

        // Update chat summary
        val summaryText = when (messageType) {
            MessageType.VOICE.name -> "🎙 پیام صوتی ($voiceDurationSeconds ثانیه)"
            MessageType.IMAGE.name -> "🖼 تصویر"
            MessageType.FILE.name -> "📁 فایل پیوست ($fileSizeMb مگابایت)"
            MessageType.POLL.name -> "📊 نظرسنجی"
            else -> content
        }
        chatDao.updateLastMessage(chatId, summaryText, now)

        return msgId
    }

    suspend fun insertChat(chat: ChatEntity): Long {
        return chatDao.insertChat(chat)
    }

    suspend fun updateChat(chat: ChatEntity) {
        chatDao.updateChat(chat)
    }

    suspend fun deleteChat(chat: ChatEntity) {
        messageDao.deleteMessagesForChat(chat.id)
        chatDao.deleteChat(chat)
    }

    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessageById(messageId)
    }

    suspend fun insertContact(contact: ContactEntity): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun checkAndSeedInitialData() {
        if (chatDao.getChatCount() == 0) {
            val now = System.currentTimeMillis()
            val timeFormat = SimpleDateFormat("HH:mm", Locale("fa"))
            val formattedTime = timeFormat.format(Date(now))

            // 1. User Profile
            if (userProfileDao.getUserProfile() == null) {
                userProfileDao.insertOrUpdateProfile(
                    UserProfileEntity(
                        id = 1,
                        name = "امیرعلی",
                        username = "amir_nika",
                        bio = "در حال استفاده از پیام‌رسان هوشمند نیکا ✨",
                        phoneNumber = "+98 912 345 6789",
                        avatarColorHex = "#0E8388",
                        isDarkTheme = true,
                        themeColorHex = "#0E8388"
                    )
                )
            }

            // 2. AI Assistant Chat
            val aiChatId = chatDao.insertChat(
                ChatEntity(
                    title = "هوش مصنوعی نیکا 🤖",
                    subtitle = "سلام! من دستیار هوشمند نیکا هستم. چطور میتونم کمکت کنم؟",
                    avatarColorHex = "#0E8388",
                    chatType = ChatType.AI_BOT.name,
                    isPinned = true,
                    isVerified = true,
                    onlineStatus = "همیشه پاسخگو",
                    bio = "دستیار هوشمند فارسی نیکا برای پاسخ به سوالات، ترجمه، خلاصه‌سازی و تولید محتوا",
                    updatedAt = now
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = aiChatId,
                    senderName = "هوش مصنوعی نیکا",
                    isFromUser = false,
                    content = "سلام امیرعلی عزیز! 👋\nمن دستیار هوشمند نیکا هستم. می‌تونی از من سوال بپرسی، خلاصه‌سازی متن بخوای یا درباره برنامه‌نویسی گفتگو کنیم!",
                    formattedTime = formattedTime,
                    timestamp = now - 600000
                )
            )

            // 3. Saved Messages
            val savedChatId = chatDao.insertChat(
                ChatEntity(
                    title = "پیام‌های ذخیره‌شده 🔖",
                    subtitle = "یادداشت‌ها و فایل‌های شخصی",
                    avatarColorHex = "#2B4C7E",
                    chatType = ChatType.DIRECT.name,
                    isPinned = true,
                    bio = "فضای شخصی برای ذخیره‌سازی پیام‌ها و یادداشت‌های شما",
                    updatedAt = now - 120000
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = savedChatId,
                    senderName = "شما",
                    isFromUser = true,
                    content = "📌 یادداشت: جلسه بررسی کد پیام‌رسان نیکا روز چهارشنبه ساعت ۱۰ صبح برگزار می‌شود.",
                    formattedTime = formattedTime,
                    timestamp = now - 120000
                )
            )

            // 4. Tech Channel
            val channelId = chatDao.insertChat(
                ChatEntity(
                    title = "کانال رسمی نیکا 🚀",
                    subtitle = "نسخه پیشرفته پیام‌رسان نیکا منتشر شد!",
                    avatarColorHex = "#0088CC",
                    chatType = ChatType.CHANNEL.name,
                    isPinned = false,
                    isVerified = true,
                    unreadCount = 2,
                    memberCount = 14200,
                    bio = "کانال اطلاعیه‌ها و اخبار رسمی پیام‌رسان نیکا",
                    updatedAt = now - 300000
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = channelId,
                    senderName = "کانال رسمی نیکا",
                    isFromUser = false,
                    content = "🎉 خوش آمدید به نسخه جدید پیام‌رسان نیکا!\n\n✨ ویژگی‌های کلیدی:\n- دستیار هوش مصنوعی فارسی مجهز به Gemini\n- رابط کاربری فوق‌العاده سریع و روان\n- پشتیبانی کامل از ذخیره‌سازی آفلاین و Room DB\n- قابلیت ثبت پیام صوتی، تصویر و فایل",
                    formattedTime = formattedTime,
                    timestamp = now - 300000
                )
            )

            // 5. Developers Group
            val groupId = chatDao.insertChat(
                ChatEntity(
                    title = "گروه توسعه‌دهندگان نیکا 💻",
                    subtitle = "رضا: رابط کاربری با Jetpack Compose عالی شده",
                    avatarColorHex = "#4A148C",
                    chatType = ChatType.GROUP.name,
                    memberCount = 28,
                    unreadCount = 1,
                    bio = "گروه هماهنگی تیم فنی و برنامه‌نویسان نیکا",
                    updatedAt = now - 500000
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = groupId,
                    senderName = "سارا",
                    isFromUser = false,
                    content = "سلام همگی! تست‌های واحد دیتابیس Room همه پاس شدند.",
                    formattedTime = formattedTime,
                    timestamp = now - 800000
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = groupId,
                    senderName = "رضا",
                    isFromUser = false,
                    content = "رابط کاربری با Jetpack Compose عالی شده 👌",
                    formattedTime = formattedTime,
                    timestamp = now - 500000
                )
            )

            // 6. Direct Contact - Maryam
            val maryamChatId = chatDao.insertChat(
                ChatEntity(
                    title = "مریم احمدی",
                    subtitle = "پروژه پیام‌رسان نیکا کاملاً آماده است؟",
                    avatarColorHex = "#E91E63",
                    chatType = ChatType.DIRECT.name,
                    onlineStatus = "آنلاین",
                    phoneNumber = "+98 912 111 2233",
                    bio = "طراح رابط و تجربه کاربری (UI/UX Designer)",
                    updatedAt = now - 900000
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = maryamChatId,
                    senderName = "مریم احمدی",
                    isFromUser = false,
                    content = "سلام امیرعلی جان، وقتت بخیر! پروژه پیام‌رسان نیکا کاملاً آماده است؟",
                    formattedTime = formattedTime,
                    timestamp = now - 900000
                )
            )

            // 7. Direct Contact - Ali
            val aliChatId = chatDao.insertChat(
                ChatEntity(
                    title = "علی رضایی",
                    subtitle = "عالیه، فردا با هم هماهنگ می‌کنیم.",
                    avatarColorHex = "#2E7D32",
                    chatType = ChatType.DIRECT.name,
                    onlineStatus = "آخرین بازدید نیم ساعت پیش",
                    phoneNumber = "+98 912 444 5566",
                    bio = "توسعه‌دهنده سیستم‌های توزیع‌شده",
                    updatedAt = now - 1800000
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = aliChatId,
                    senderName = "علی رضایی",
                    isFromUser = false,
                    content = "عالیه، فردا با هم هماهنگ می‌کنیم.",
                    formattedTime = formattedTime,
                    timestamp = now - 1800000
                )
            )

            // 8. Seed Contacts
            if (contactDao.getContactCount() == 0) {
                contactDao.insertContact(
                    ContactEntity(
                        name = "مریم احمدی",
                        phoneNumber = "+98 912 111 2233",
                        bio = "طراح UI/UX",
                        avatarColorHex = "#E91E63",
                        isOnline = true
                    )
                )
                contactDao.insertContact(
                    ContactEntity(
                        name = "علی رضایی",
                        phoneNumber = "+98 912 444 5566",
                        bio = "توسعه‌دهنده سیستم",
                        avatarColorHex = "#2E7D32",
                        isOnline = false,
                        lastSeen = "۳۰ دقیقه پیش"
                    )
                )
                contactDao.insertContact(
                    ContactEntity(
                        name = "سارا محمدی",
                        phoneNumber = "+98 912 777 8899",
                        bio = "مدیر محصول",
                        avatarColorHex = "#8E24AA",
                        isOnline = true
                    )
                )
                contactDao.insertContact(
                    ContactEntity(
                        name = "پشتیبانی نیکا",
                        phoneNumber = "+98 21 8888 0000",
                        bio = "پشتیبانی ۲۴ ساعته کاربران نیکا",
                        avatarColorHex = "#0E8388",
                        isOnline = true
                    )
                )
            }
        }
    }
}
