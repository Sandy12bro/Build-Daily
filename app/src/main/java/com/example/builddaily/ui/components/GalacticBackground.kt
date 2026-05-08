package com.example.builddaily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float
)

@Composable
fun GalacticBackground() {
    val stars = remember {
        List(100) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = 1f + Random.nextFloat() * 3f,
                alpha = 0.1f + Random.nextFloat() * 0.5f,
                speed = 0.0005f + Random.nextFloat() * 0.001f
            )
        }
    }

    var frameTime by remember { mutableLongStateOf(0L) }
    val infiniteTransition = rememberInfiniteTransition(label = "StarTwinkle")
    
    // Smooth twinkling effect
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Twinkle"
    )

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frameTime = it }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.7f)) {
        val width = size.width
        val height = size.height

        stars.forEach { star ->
            // Move stars slowly downwards for a feeling of floating through space
            val currentY = (star.y + (frameTime * star.speed / 100f)) % 1.0f
            
            drawCircle(
                color = Color.White.copy(alpha = star.alpha * twinkleAlpha),
                radius = star.size,
                center = Offset(star.x * width, currentY * height)
            )
        }
    }
}
