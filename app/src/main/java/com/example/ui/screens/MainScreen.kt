package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ChatType
import com.example.ui.theme.NikaMessengerTheme
import com.example.ui.theme.NikaTealPrimary
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NikaViewModel

@Composable
fun MainScreen(viewModel: NikaViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val filteredChats by viewModel.filteredChats.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val contactSearchQuery by viewModel.contactSearchQuery.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val currentChat by viewModel.currentChat.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    val isDarkTheme = userProfile?.isDarkTheme ?: true
    val themeAccent = try {
        Color(android.graphics.Color.parseColor(userProfile?.themeColorHex ?: "#0E8388"))
    } catch (e: Exception) {
        NikaTealPrimary
    }

    NikaMessengerTheme(
        darkTheme = isDarkTheme,
        primaryAccent = themeAccent
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (selectedChatId != null && currentChat != null) {
                // Opened Chat Screen Fullscreen
                ChatDetailScreen(
                    chat = currentChat!!,
                    messages = currentMessages,
                    isAiThinking = isAiThinking,
                    onBackClick = { viewModel.closeChat() },
                    onSendMessage = { text ->
                        viewModel.sendTextMessage(currentChat!!.id, text)
                    },
                    onSendVoice = { duration ->
                        viewModel.sendVoiceMessage(currentChat!!.id, duration)
                    },
                    onSendFile = { fileName, size ->
                        viewModel.sendFileMessage(currentChat!!.id, fileName, size)
                    },
                    onSendImage = { caption ->
                        viewModel.sendImageMessage(currentChat!!.id, caption)
                    },
                    onSendPoll = { question, options ->
                        viewModel.sendPollMessage(currentChat!!.id, question, options)
                    },
                    onDeleteMessage = { msgId ->
                        viewModel.deleteMessage(msgId)
                    },
                    onToggleMute = {
                        viewModel.toggleMuteChat(currentChat!!)
                    },
                    onDeleteChat = {
                        viewModel.deleteChat(currentChat!!)
                    }
                )
            } else {
                // Main Navigation Screen with Bottom Navigation
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = activeTab == MainTab.CHATS,
                                onClick = { viewModel.setActiveTab(MainTab.CHATS) },
                                icon = { Icon(imageVector = Icons.Default.Chat, contentDescription = "گفتگوها") },
                                label = { Text("گفتگوها") },
                                modifier = Modifier.testTag("tab_chats")
                            )

                            NavigationBarItem(
                                selected = activeTab == MainTab.CONTACTS,
                                onClick = { viewModel.setActiveTab(MainTab.CONTACTS) },
                                icon = { Icon(imageVector = Icons.Default.People, contentDescription = "مخاطبین") },
                                label = { Text("مخاطبین") },
                                modifier = Modifier.testTag("tab_contacts")
                            )

                            NavigationBarItem(
                                selected = activeTab == MainTab.SAVED,
                                onClick = { viewModel.setActiveTab(MainTab.SAVED) },
                                icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "پیام‌های ذخیره‌شده") },
                                label = { Text("ذخیره‌شده‌ها") },
                                modifier = Modifier.testTag("tab_saved")
                            )

                            NavigationBarItem(
                                selected = activeTab == MainTab.SETTINGS,
                                onClick = { viewModel.setActiveTab(MainTab.SETTINGS) },
                                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "تنظیمات") },
                                label = { Text("تنظیمات") },
                                modifier = Modifier.testTag("tab_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            MainTab.CHATS -> ChatListScreen(
                                chats = filteredChats,
                                selectedCategory = selectedCategory,
                                onSelectCategory = { viewModel.setSelectedCategory(it) },
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onChatClick = { chatId -> viewModel.openChat(chatId) },
                                onCreateChat = { title, type, phone, bio ->
                                    viewModel.createNewChat(title, type, phone, bio)
                                },
                                onPinChat = { viewModel.togglePinChat(it) },
                                onMuteChat = { viewModel.toggleMuteChat(it) },
                                onDeleteChat = { viewModel.deleteChat(it) }
                            )

                            MainTab.CONTACTS -> ContactsScreen(
                                contacts = filteredContacts,
                                searchQuery = contactSearchQuery,
                                onSearchChange = { viewModel.setContactSearchQuery(it) },
                                onAddContact = { name, phone, bio ->
                                    viewModel.addNewContact(name, phone, bio)
                                },
                                onStartChat = { contact ->
                                    viewModel.createNewChat(contact.name, ChatType.DIRECT, contact.phoneNumber, contact.bio)
                                }
                            )

                            MainTab.SAVED -> SavedMessagesScreen(
                                onOpenSavedChat = {
                                    val savedChat = filteredChats.find { it.title.contains("ذخیره‌شده") }
                                    if (savedChat != null) {
                                        viewModel.openChat(savedChat.id)
                                    } else {
                                        viewModel.createNewChat("پیام‌های ذخیره‌شده 🔖", ChatType.DIRECT, "", "فضای شخصی")
                                    }
                                }
                            )

                            MainTab.SETTINGS -> SettingsScreen(
                                userProfile = userProfile,
                                onUpdateProfile = { name, username, bio, phone, isDark, themeHex ->
                                    viewModel.updateUserProfile(name, username, bio, phone, isDark, themeHex)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
