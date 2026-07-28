package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS,
    CONTACTS,
    SAVED,
    SETTINGS
}

class NikaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NikaRepository

    init {
        val database = NikaDatabase.getDatabase(application)
        repository = NikaRepository(database)

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Active Main Tab
    private val _activeTab = MutableStateFlow(MainTab.CHATS)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
    }

    // Selected Category Filter for Chats
    private val _selectedCategory = MutableStateFlow("همه")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Contact Search Query
    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    fun setContactSearchQuery(query: String) {
        _contactSearchQuery.value = query
    }

    // Filtered Chats Flow
    val filteredChats: StateFlow<List<ChatEntity>> = combine(
        repository.allChats,
        _selectedCategory,
        _searchQuery
    ) { chats, category, query ->
        chats.filter { chat ->
            val matchesCategory = when (category) {
                "شخصی" -> chat.chatType == ChatType.DIRECT.name
                "گروه‌ها" -> chat.chatType == ChatType.GROUP.name
                "کانال‌ها" -> chat.chatType == ChatType.CHANNEL.name
                "هوش مصنوعی" -> chat.chatType == ChatType.AI_BOT.name
                else -> true
            }

            val matchesQuery = query.isEmpty() ||
                    chat.title.contains(query, ignoreCase = true) ||
                    chat.subtitle.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Contacts Flow
    val filteredContacts: StateFlow<List<ContactEntity>> = combine(
        repository.allContacts,
        _contactSearchQuery
    ) { contacts, query ->
        if (query.isEmpty()) {
            contacts
        } else {
            contacts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Profile Flow
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Currently Selected Chat
    private val _selectedChatId = MutableStateFlow<Long?>(null)
    val selectedChatId: StateFlow<Long?> = _selectedChatId.asStateFlow()

    fun openChat(chatId: Long) {
        _selectedChatId.value = chatId
        viewModelScope.launch {
            repository.clearUnreadCount(chatId)
        }
    }

    fun closeChat() {
        _selectedChatId.value = null
    }

    val currentChat: StateFlow<ChatEntity?> = _selectedChatId.flatMapLatest { id ->
        if (id != null) {
            repository.getChatByIdFlow(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMessages: StateFlow<List<MessageEntity>> = _selectedChatId.flatMapLatest { id ->
        if (id != null) {
            repository.getMessagesForChat(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Bot Generating Indicator
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Message Actions
    fun sendTextMessage(chatId: Long, text: String, replyToText: String? = null) {
        if (text.isBlank()) return
        val currentProfile = userProfile.value
        val userName = currentProfile?.name ?: "شما"

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderName = userName,
                content = text.trim(),
                isFromUser = true,
                messageType = MessageType.TEXT.name,
                replyToText = replyToText
            )

            // Check if this chat is AI Bot
            val chat = currentChat.value
            if (chat?.chatType == ChatType.AI_BOT.name) {
                handleAiBotReply(chatId, text.trim())
            } else if (chat?.chatType == ChatType.DIRECT.name && chat.title != "پیام‌های ذخیره‌شده 🔖") {
                // Simulate partner typing & auto-reply after 2 seconds
                simulatePartnerReply(chatId, chat.title)
            }
        }
    }

    fun sendVoiceMessage(chatId: Long, durationSeconds: Int) {
        val currentProfile = userProfile.value
        val userName = currentProfile?.name ?: "شما"

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderName = userName,
                content = "🎙 پیام صوتی ($durationSeconds ثانیه)",
                isFromUser = true,
                messageType = MessageType.VOICE.name,
                voiceDurationSeconds = durationSeconds
            )
        }
    }

    fun sendFileMessage(chatId: Long, fileName: String, sizeMb: Double) {
        val currentProfile = userProfile.value
        val userName = currentProfile?.name ?: "شما"

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderName = userName,
                content = "📄 فایل: $fileName",
                isFromUser = true,
                messageType = MessageType.FILE.name,
                fileSizeMb = sizeMb
            )
        }
    }

    fun sendImageMessage(chatId: Long, caption: String = "تصویر ارسالی") {
        val currentProfile = userProfile.value
        val userName = currentProfile?.name ?: "شما"

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderName = userName,
                content = caption,
                isFromUser = true,
                messageType = MessageType.IMAGE.name
            )
        }
    }

    fun sendPollMessage(chatId: Long, question: String, options: List<String>) {
        val currentProfile = userProfile.value
        val userName = currentProfile?.name ?: "شما"
        val pollContent = "📊 $question\n" + options.joinToString("\n") { "• $it" }

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderName = userName,
                content = pollContent,
                isFromUser = true,
                messageType = MessageType.POLL.name
            )
        }
    }

    private suspend fun handleAiBotReply(chatId: Long, userText: String) {
        _isAiThinking.value = true
        delay(1200) // Realistic AI generation delay

        val lowerMsg = userText.lowercase()
        val aiResponse = when {
            lowerMsg.contains("سلام") || lowerMsg.contains("درود") ->
                "سلام! 👋 من دستیار هوشمند نیکا هستم. امروزه چطور می‌تونم بهت کمک کنم؟"
            lowerMsg.contains("نیکا") || lowerMsg.contains("چیست") ->
                "پیام‌رسان نیکا یک پیام‌رسان هوشمند، سریع و مدرن فارسی است که از ارتباطات مستقیم، گروه‌ها، کانال‌ها و دستیار هوش مصنوعی پشتیبانی می‌کند."
            lowerMsg.contains("ویژگی") || lowerMsg.contains("قابلیت") ->
                "🌟 قابلیت‌های نیکا:\n۱. سرعت بالای ارسال پیام و فایل\n۲. دستیار هوشمند اختصاصی\n۳. امنیت و ذخیره‌سازی محلی با Room\n۴. پشتیبانی از تم‌های جذاب و حالت شب"
            lowerMsg.contains("کد") || lowerMsg.contains("برنامه‌نویسی") || lowerMsg.contains("برنامه") ->
                "💻 نیکا با استفاده از زبان Kotlin و فریم‌ورک مدرن Jetpack Compose پیاده‌سازی شده است و تمامی اطلاعات پیام‌ها به صورت امن در دیتابیس Room ذخیره می‌شوند."
            lowerMsg.contains("شعر") || lowerMsg.contains("متن") ->
                "🌸 «ای نام تو بهترین سرآغاز / بی‌نام تو نامه کی کنم باز»\nچطور می‌تونم در نگارش متن یا ایده‌پردازی بهت کمک کنم؟"
            else ->
                "بسیار عالی! در پاسخ به «$userText»:\nمن می‌توانم به صورت کامل در حوزه‌های برنامه‌نویسی، خلاصه‌سازی، ترجمه و پاسخ به پرسش‌های روزمره همراهت باشم."
        }

        _isAiThinking.value = false
        repository.sendMessage(
            chatId = chatId,
            senderName = "هوش مصنوعی نیکا",
            content = aiResponse,
            isFromUser = false,
            messageType = MessageType.TEXT.name
        )
    }

    private suspend fun simulatePartnerReply(chatId: Long, partnerName: String) {
        delay(2500)
        val replies = listOf(
            "ممنون از پیامت! بررسی می‌کنم و خبرت میدم 👍",
            "عالیه! موافقم.",
            "باشه حتماً، فردا درباره‌اش صحبت می‌کنیم.",
            "ممنون، پیام دریافت شد ✨"
        )
        val randomReply = replies.random()

        repository.sendMessage(
            chatId = chatId,
            senderName = partnerName,
            content = randomReply,
            isFromUser = false,
            messageType = MessageType.TEXT.name
        )
    }

    // Chat Management
    fun createNewChat(title: String, chatType: ChatType, phoneNumber: String = "", bio: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val colorHexes = listOf("#0E8388", "#0088CC", "#2E7D32", "#E91E63", "#8E24AA", "#F59E0B")
            val newChat = ChatEntity(
                title = title.trim(),
                subtitle = if (chatType == ChatType.CHANNEL) "کانال تازه ایجاد شده" else "گفتگوی جدید",
                avatarColorHex = colorHexes.random(),
                chatType = chatType.name,
                phoneNumber = phoneNumber,
                bio = bio.ifEmpty { "توضیحات گفتگو" },
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.insertChat(newChat)
            openChat(newId)
        }
    }

    fun addNewContact(name: String, phoneNumber: String, bio: String) {
        if (name.isBlank() || phoneNumber.isBlank()) return
        viewModelScope.launch {
            val contact = ContactEntity(
                name = name.trim(),
                phoneNumber = phoneNumber.trim(),
                bio = bio.trim(),
                avatarColorHex = "#0E8388",
                isOnline = true
            )
            repository.insertContact(contact)

            // Also create direct chat
            createNewChat(name, ChatType.DIRECT, phoneNumber, bio)
        }
    }

    fun togglePinChat(chat: ChatEntity) {
        viewModelScope.launch {
            repository.updateChat(chat.copy(isPinned = !chat.isPinned))
        }
    }

    fun toggleMuteChat(chat: ChatEntity) {
        viewModelScope.launch {
            repository.updateChat(chat.copy(isMuted = !chat.isMuted))
        }
    }

    fun deleteChat(chat: ChatEntity) {
        viewModelScope.launch {
            if (_selectedChatId.value == chat.id) {
                closeChat()
            }
            repository.deleteChat(chat)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun updateUserProfile(
        name: String,
        username: String,
        bio: String,
        phone: String,
        isDark: Boolean,
        themeHex: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            val updated = current.copy(
                name = name,
                username = username,
                bio = bio,
                phoneNumber = phone,
                isDarkTheme = isDark,
                themeColorHex = themeHex
            )
            repository.updateUserProfile(updated)
        }
    }
}
