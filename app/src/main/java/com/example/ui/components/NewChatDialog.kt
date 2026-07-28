package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.ChatType

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onCreateChat: (title: String, type: ChatType, phone: String, bio: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ChatType.DIRECT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ایجاد گفتگو یا کانال جدید",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterChip(
                        selected = selectedType == ChatType.DIRECT,
                        onClick = { selectedType = ChatType.DIRECT },
                        label = { Text("گفتگو") }
                    )
                    FilterChip(
                        selected = selectedType == ChatType.GROUP,
                        onClick = { selectedType = ChatType.GROUP },
                        label = { Text("گروه") }
                    )
                    FilterChip(
                        selected = selectedType == ChatType.CHANNEL,
                        onClick = { selectedType = ChatType.CHANNEL },
                        label = { Text("کانال") }
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (selectedType == ChatType.DIRECT) "نام مخاطب" else "نام گروه / کانال") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_title_input")
                )

                if (selectedType == ChatType.DIRECT) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("شماره همراه") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_chat_phone_input")
                    )
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("توضیحات / بیوگرافی") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateChat(title, selectedType, phoneNumber, bio)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_chat_button")
            ) {
                Text("ایجاد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
