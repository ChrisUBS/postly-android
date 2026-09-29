package com.example.postly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PostlyBlue,
    onPrimary = Color.White,
    secondary = PostlyBlue,
    background = Color(0xFFF8F9FB),
    surface = Color.White,
    onBackground = PostlyText,
    onSurface = PostlyText,
    error = Color(0xFFFF3B30)
)

@Composable
fun PostlyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
