package com.example.builddaily.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset

private val OnyxColorScheme = darkColorScheme(
    primary = BlueprintLavender,
    secondary = Slate400,
    tertiary = ConstructionEmerald,
    background = OnyxBlack,
    surface = SurfaceGrey,
    onPrimary = OnyxBlack,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = GlassGrey,
    outline = Slate400,
    error = ErrorRose
)

@Composable
fun BuildDailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to maintain the premium Onyx aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = OnyxColorScheme // We force the Onyx theme for premium feel

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun NebulaBackground() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CyberPurple.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.3f),
                radius = 800f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElectricBlue.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.7f),
                radius = 1000f
            )
        )
    }
}