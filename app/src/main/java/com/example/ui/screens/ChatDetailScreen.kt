package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatEntity
import com.example.data.local.ChatType
import com.example.data.local.MessageEntity
import com.example.ui.components.AttachmentSheet
import com.example.ui.components.ChatInputBar
import com.example.ui.components.MessageBubble
import com.example.ui.theme.NikaTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: ChatEntity,
    messages: List<MessageEntity>,
    isAiThinking: Boolean,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendVoice: (Int) -> Unit,
    onSendFile: (String, Double) -> Unit,
    onSendImage: (String) -> Unit,
    onSendPoll: (String, List<String>) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onToggleMute: () -> Unit,
    onDeleteChat: () -> Unit
) {
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var replyMessage by remember { mutableStateOf<MessageEntity?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size, isAiThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val parsedColor = try {
        Color(android.graphics.Color.parseColor(chat.avatarColorHex))
    } catch (e: Exception) {
        NikaTealPrimary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (chat.chatType == ChatType.AI_BOT.name) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_nika_ai_avatar),
                                    contentDescription = "AI Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(parsedColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chat.title.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isAiThinking) "در حال پاسخگویی..." else when (chat.chatType) {
                                    ChatType.CHANNEL.name -> "${chat.memberCount} دنبال‌کننده"
                                    ChatType.GROUP.name -> "${chat.memberCount} عضو"
                                    else -> chat.onlineStatus
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAiThinking) NikaTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_from_chat_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "گزینه‌ها")
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (chat.isMuted) "فعال‌سازی صدا" else "بی‌صدا کردن") },
                            onClick = {
                                onToggleMute()
                                showMoreMenu = false
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.VolumeOff, contentDescription = null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("حذف گفتگو") },
                            onClick = {
                                onDeleteChat()
                                showMoreMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                onSendMessage = { text ->
                    onSendMessage(text)
                    replyMessage = null
                },
                onOpenAttachment = { showAttachmentSheet = true },
                onSendVoiceSimulator = {
                    onSendVoice(12)
                },
                replyText = replyMessage?.content,
                onCancelReply = { replyMessage = null }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onDelete = { onDeleteMessage(msg.id) },
                            onReply = { replyMessage = msg }
                        )
                    }

                    if (isAiThinking) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = NikaTealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "هوش مصنوعی نیکا در حال تفکر...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Sheet Modal
    if (showAttachmentSheet) {
        AttachmentSheet(
            onDismiss = { showAttachmentSheet = false },
            onSendImage = { onSendImage("تصویر نمونه پیوست شده") },
            onSendVoice = { onSendVoice(18) },
            onSendFile = { onSendFile("Document_Nika.pdf", 4.2) },
            onSendPoll = {
                onSendPoll(
                    "آیا از عملکرد نیکا راضی هستید؟",
                    listOf("بله بسیار عالی", "خوب است", "نیاز به بهینه‌سازی")
                )
            }
        )
    }
}
