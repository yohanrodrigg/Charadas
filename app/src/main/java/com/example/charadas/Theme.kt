package com.example.charadas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Night = Color(0xFF1B1033)
val NightCard = Color(0xFF2B1B4D)
val Accent = Color(0xFFFFC145)
val Correct = Color(0xFF2E9E5B)
val Pass = Color(0xFFE07A1F)
val TextLight = Color(0xFFF5F0FF)
val TextMuted = Color(0xFFB9AAD6)

private val CharadasColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF2A1A00),
    background = Night,
    onBackground = TextLight,
    surface = NightCard,
    onSurface = TextLight
)

@Composable
fun CharadasTheme(content: @Composable () -> Unit) {
    // La app usa siempre el tema oscuro: es un juego para jugar en grupo.
    MaterialTheme(colorScheme = CharadasColors, content = content)
}
