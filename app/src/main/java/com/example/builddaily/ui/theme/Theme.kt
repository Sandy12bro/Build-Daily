package com.example.builddaily.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Gray600,
    onSecondary = White,
    tertiary = Gray700,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = Gray50,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    outlineVariant = Gray200
)

@Composable
fun BuildDailyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}