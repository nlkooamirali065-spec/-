package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NikaSleekBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = NikaSleekBluePrimary,
    onSecondary = Color.White,
    background = NikaTealDarkBg,
    onBackground = Color(0xFFF8FAFC),
    surface = NikaCardDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = NikaSleekBluePrimary,
    onPrimary = Color.White,
    primaryContainer = NikaSleekBlueContainer,
    onPrimaryContainer = NikaSleekOnBlueContainer,
    secondary = NikaSleekBluePrimary,
    onSecondary = Color.White,
    background = NikaSleekLightBg,
    onBackground = NikaSleekTextDark,
    surface = Color.White,
    onSurface = NikaSleekTextDark,
    surfaceVariant = NikaSleekLightSurface,
    onSurfaceVariant = NikaSleekTextSubtle,
    outline = NikaSleekBorder
)

@Composable
fun NikaMessengerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryAccent: Color = NikaSleekBluePrimary,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customScheme = baseScheme.copy(
        primary = primaryAccent,
        secondary = primaryAccent
    )

    MaterialTheme(
        colorScheme = customScheme,
        typography = Typography,
        content = content
    )
}
