package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.NikaTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfileEntity?,
    onUpdateProfile: (name: String, username: String, bio: String, phone: String, isDark: Boolean, themeHex: String) -> Unit
) {
    var isDarkTheme by remember(userProfile) { mutableStateOf(userProfile?.isDarkTheme ?: true) }
    var notificationsEnabled by remember { mutableStateOf(userProfile?.notificationsEnabled ?: true) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    var nameInput by remember(userProfile) { mutableStateOf(userProfile?.name ?: "کاربر نیکا") }
    var usernameInput by remember(userProfile) { mutableStateOf(userProfile?.username ?: "nika_user") }
    var bioInput by remember(userProfile) { mutableStateOf(userProfile?.bio ?: "") }
    var phoneInput by remember(userProfile) { mutableStateOf(userProfile?.phoneNumber ?: "") }

    val themeColors = listOf(
        "#0E8388" to "فیروزه‌ای نیکا",
        "#0284C7" to "آبی نیلگون",
        "#7C3AED" to "بنفش سلطنتی",
        "#059669" to "زمردی"
    )

    var selectedThemeHex by remember(userProfile) { mutableStateOf(userProfile?.themeColorHex ?: "#0E8388") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات نیکا", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(NikaTealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.name ?: "ن").take(1).uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.name ?: "کاربر نیکا",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userProfile?.phoneNumber ?: "+98 912 345 6789",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@${userProfile?.username ?: "nika_user"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NikaTealPrimary
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "ویرایش")
                    }
                }
            }

            // Appearance & Theme Options
            Text(
                text = "ظاهر و پوسته",
                style = MaterialTheme.typography.titleSmall,
                color = NikaTealPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("حالت شب (تاریک)")
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { checked ->
                                isDarkTheme = checked
                                onUpdateProfile(
                                    userProfile?.name ?: "کاربر نیکا",
                                    userProfile?.username ?: "nika_user",
                                    userProfile?.bio ?: "",
                                    userProfile?.phoneNumber ?: "",
                                    checked,
                                    selectedThemeHex
                                )
                            },
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("رنگ پوسته اصلی:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        themeColors.forEach { (hex, name) ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                NikaTealPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        selectedThemeHex = hex
                                        onUpdateProfile(
                                            userProfile?.name ?: "کاربر نیکا",
                                            userProfile?.username ?: "nika_user",
                                            userProfile?.bio ?: "",
                                            userProfile?.phoneNumber ?: "",
                                            isDarkTheme,
                                            hex
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedThemeHex == hex) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notifications & Privacy Settings
            Text(
                text = "اعلان‌ها و امنیت",
                style = MaterialTheme.typography.titleSmall,
                color = NikaTealPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("اعلان‌های پیام جدید")
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("قفل با رمز عبور")
                        }
                        Text("غیرفعال", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Storage Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = NikaTealPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("حافظه و دیتابیس محلی", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("حجم پیام‌های ذخیره‌شده: ۴.۲ مگابایت", fontSize = 12.sp)
                    Text("حافظه کش: ۱.۸ مگابایت", fontSize = 12.sp)
                }
            }

            // About Nika
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NikaTealPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("درباره پیام‌رسان نیکا", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "پیام‌رسان نیکا نسخه ۲.۴.۰ - توسعه داده شده با Jetpack Compose و Room DB",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("ویرایش پروفایل") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("نام و نام خانوادگی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                    )
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("نام کاربری") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_username")
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("شماره همراه") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone")
                    )
                    OutlinedTextField(
                        value = bioInput,
                        onValueChange = { bioInput = it },
                        label = { Text("بیوگرافی") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_bio")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(
                            nameInput,
                            usernameInput,
                            bioInput,
                            phoneInput,
                            isDarkTheme,
                            selectedThemeHex
                        )
                        showEditProfileDialog = false
                    },
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
