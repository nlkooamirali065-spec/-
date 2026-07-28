package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentSheet(
    onDismiss: () -> Unit,
    onSendImage: () -> Unit,
    onSendVoice: () -> Unit,
    onSendFile: () -> Unit,
    onSendPoll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "افزودن پیوست به پیام",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    title = "تصویر",
                    icon = Icons.Default.Image,
                    bgColor = Color(0xFF0284C7),
                    onClick = {
                        onSendImage()
                        onDismiss()
                    }
                )

                AttachmentItem(
                    title = "پیام صوتی",
                    icon = Icons.Default.Mic,
                    bgColor = Color(0xFF059669),
                    onClick = {
                        onSendVoice()
                        onDismiss()
                    }
                )

                AttachmentItem(
                    title = "فایل",
                    icon = Icons.Default.InsertDriveFile,
                    bgColor = Color(0xFF7C3AED),
                    onClick = {
                        onSendFile()
                        onDismiss()
                    }
                )

                AttachmentItem(
                    title = "نظرسنجی",
                    icon = Icons.Default.Poll,
                    bgColor = Color(0xFFF59E0B),
                    onClick = {
                        onSendPoll()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AttachmentItem(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
