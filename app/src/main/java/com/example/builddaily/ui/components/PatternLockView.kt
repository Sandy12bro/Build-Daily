package com.example.builddaily.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    onPatternComplete: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.3f),
    activeColor: Color = Color(0xFF8B5CF6),
    gridSize: Int = 3
) {
    var selectedDots by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    BoxWithConstraints(modifier = modifier.aspectRatio(1f).padding(24.dp)) {
        val width = constraints.maxWidth.toFloat()
        val cellSize = width / gridSize
        val radius = 10.dp.value

        val dotCenters = remember(width) {
            List(gridSize * gridSize) { i ->
                val row = i / gridSize
                val col = i % gridSize
                Offset(
                    x = col * cellSize + cellSize / 2,
                    y = row * cellSize + cellSize / 2
                )
            }
        }

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedDots = emptyList()
                        currentTouchPoint = offset
                        val hit = findHit(offset, dotCenters, radius * 2)
                        if (hit != -1) selectedDots = listOf(hit)
                    },
                    onDrag = { change, _ ->
                        currentTouchPoint = change.position
                        val hit = findHit(change.position, dotCenters, radius * 2)
                        if (hit != -1 && hit !in selectedDots) {
                            selectedDots = selectedDots + hit
                        }
                    },
                    onDragEnd = {
                        onPatternComplete(selectedDots)
                        selectedDots = emptyList()
                        currentTouchPoint = null
                    }
                )
            }
        ) {
            // Draw Dots
            dotCenters.forEachIndexed { index, center ->
                val isActive = index in selectedDots
                drawCircle(
                    color = if (isActive) activeColor else dotColor,
                    radius = if (isActive) radius * 1.5f else radius,
                    center = center
                )
                if (isActive) {
                    drawCircle(
                        color = activeColor.copy(alpha = 0.2f),
                        radius = radius * 3f,
                        center = center
                    )
                }
            }

            // Draw Lines
            if (selectedDots.isNotEmpty()) {
                for (i in 0 until selectedDots.size - 1) {
                    drawLine(
                        color = activeColor,
                        start = dotCenters[selectedDots[i]],
                        end = dotCenters[selectedDots[i + 1]],
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                currentTouchPoint?.let { touch ->
                    drawLine(
                        color = activeColor,
                        start = dotCenters[selectedDots.last()],
                        end = touch,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun findHit(offset: Offset, centers: List<Offset>, threshold: Float): Int {
    centers.forEachIndexed { index, center ->
        val dist = sqrt((offset.x - center.x) * (offset.x - center.x) + (offset.y - center.y) * (offset.y - center.y))
        if (dist < threshold) return index
    }
    return -1
}
