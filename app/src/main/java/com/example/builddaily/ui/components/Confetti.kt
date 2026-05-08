package com.example.builddaily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.builddaily.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiPiece(
    var x: Float,
    var y: Float,
    var color: Color,
    var speed: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var size: Float,
    var side: Int // 0 for square, 1 for circle
)

@Composable
fun ConfettiCelebration(onFinished: () -> Unit) {
    val colors = listOf(
        ElectricBlue, CyberPurple, MintGreen, BerryPink, 
        Color(0xFFFFD700), Color(0xFF00FFFF), Color(0xFFFF00FF)
    )
    
    val pieces = remember {
        List(80) {
            ConfettiPiece(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.6f,
                color = colors.random(),
                speed = 400f + Random.nextFloat() * 800f, // pixels per second
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 360f, // degrees per second
                size = 20f + Random.nextFloat() * 30f,
                side = Random.nextInt(2)
            )
        }
    }

    var lastTime by remember { mutableLongStateOf(0L) }
    var timeElapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (timeElapsed < 5000) { // 5 seconds
            withFrameMillis { frameTime ->
                if (lastTime != 0L) {
                    val delta = (frameTime - lastTime) / 1000f
                    pieces.forEach { piece ->
                        piece.y += (piece.speed / 1000f) * delta // Normalize speed
                        piece.rotation += piece.rotationSpeed * delta
                        piece.x += (kotlin.math.sin(piece.y * 10f) * 0.05f * delta).toFloat()
                    }
                    timeElapsed += (frameTime - lastTime)
                }
                lastTime = frameTime
            }
        }
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // This is to force recomposition every frame
        val _timer = timeElapsed 
        
        val width = size.width
        val height = size.height

        pieces.forEach { piece ->
            if (piece.y > 1.2f) return@forEach

            val drawX = piece.x * width
            val drawY = piece.y * height

            withTransform({
                rotate(piece.rotation, Offset(drawX, drawY))
            }) {
                if (piece.side == 0) {
                    drawRect(
                        color = piece.color,
                        topLeft = Offset(drawX - piece.size / 2, drawY - piece.size / 2),
                        size = Size(piece.size, piece.size * 0.6f)
                    )
                } else {
                    drawCircle(
                        color = piece.color,
                        center = Offset(drawX, drawY),
                        radius = piece.size / 2
                    )
                }
            }
        }
    }
}
