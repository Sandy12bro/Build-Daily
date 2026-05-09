package com.example.builddaily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.UserStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LifeArchitectureHUD(stats: UserStats) {
    val factor = stats.growthFactor
    val growthValue = if (factor.isNaN() || factor.isInfinite()) 0.01f else factor
    
    // Animation States
    val trunkAnim = remember { Animatable(0f) }
    val canopyAnim = remember { Animatable(0f) }
    val mistAnim = remember { Animatable(0f) }
    val godRaysAnim = remember { Animatable(0f) }

    // Ambient Effects
    val infiniteTransition = rememberInfiniteTransition(label = "majestic_nature")
    val slowSway by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slow_sway"
    )
    
    val rayPulse by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ray_pulse"
    )

    LaunchedEffect(growthValue) {
        mistAnim.animateTo(1f, tween(3000))
        trunkAnim.animateTo(
            targetValue = (growthValue / 0.35f).coerceIn(0f, 1f),
            animationSpec = tween(2500, easing = FastOutSlowInEasing)
        )
        if (growthValue > 0.3f) {
            canopyAnim.animateTo(
                targetValue = ((growthValue - 0.3f) / 0.7f).coerceIn(0f, 1f),
                animationSpec = tween(3000, easing = FastOutSlowInEasing)
            )
            godRaysAnim.animateTo(1f, tween(4000))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
            contentAlignment = Alignment.Center
        ) {
            val availableWidth = constraints.maxWidth.toFloat()
            val availableHeight = constraints.maxHeight.toFloat()
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val centerX = canvasW / 2f
                val bottomY = canvasH * 0.95f

                // 1. GOD RAYS
                if (godRaysAnim.value > 0.1f) {
                    drawGodRays(centerX, canvasH * 0.3f, godRaysAnim.value * rayPulse)
                }

                // 2. ROOT MIST
                drawMajesticMist(centerX, bottomY, mistAnim.value)

                // 3. THE ANCIENT TREE
                withTransform({
                    rotate(slowSway, pivot = Offset(centerX, bottomY))
                }) {
                    // Responsive Tapered Trunk
                    val tVal = trunkAnim.value.coerceIn(0.001f, 1f)
                    val tHeight = canvasH * 0.6f * tVal
                    val tWidth = (canvasW * 0.08f) * growthValue.coerceAtLeast(0.2f)
                    
                    drawAncientTrunk(centerX, bottomY, tHeight, tWidth)

                    // ROOTS
                    if (tVal > 0.2f) {
                        drawVisibleRoots(centerX, bottomY, tWidth, tVal)
                    }

                    // MASSIVE CANOPY
                    if (canopyAnim.value > 0.01f) {
                        val trunkTop = Offset(centerX, bottomY - tHeight)
                        drawAncientCanopy(trunkTop, canopyAnim.value, slowSway, canvasW)
                    }
                }

                // 4. PARTICLES
                drawAtmosphericParticles(centerX, canvasH * 0.4f, canopyAnim.value)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = getAncientStageName(stats.daysActive).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 4.sp
        )
        
        Text(
            text = "EVOLUTION CYCLE DAY ${stats.daysActive}",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF00BFA5).copy(alpha = 0.5f),
            letterSpacing = 2.sp
        )
    }
}

fun DrawScope.drawGodRays(x: Float, y: Float, alpha: Float) {
    val random = java.util.Random(42)
    val rayCount = 12
    
    // Start from well outside the screen to ensure full coverage even with tilt
    val totalWidth = size.width * 2.5f
    val startOffset = -size.width * 0.8f
    
    repeat(rayCount) { i ->
        val rayAlpha = (alpha * (0.08f + random.nextFloat() * 0.12f)).coerceIn(0f, 0.2f)
        val startX = startOffset + (i * (totalWidth / rayCount))
        val rayWidth = 60f + random.nextFloat() * 150f
        
        val rayPath = Path().apply {
            moveTo(startX, -100f)
            lineTo(startX + size.width * 0.5f, size.height + 100f)
            lineTo(startX + size.width * 0.5f + rayWidth, size.height + 100f)
            lineTo(startX + rayWidth, -100f)
            close()
        }
        
        drawPath(
            path = rayPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFDE7).copy(alpha = rayAlpha), 
                    Color(0xFFE1F5FE).copy(alpha = rayAlpha * 0.4f),
                    Color.Transparent
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + size.width * 0.4f, size.height)
            )
        )
    }
}

fun DrawScope.drawMajesticMist(x: Float, y: Float, progress: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.Transparent, Color(0xFF1B1B1B).copy(alpha = 0.4f * progress), Color(0xFF263238).copy(alpha = 0.6f * progress))
        ),
        topLeft = Offset(0f, y - 120f),
        size = Size(size.width, 140f)
    )
}

fun DrawScope.drawAncientTrunk(x: Float, y: Float, h: Float, w: Float) {
    val trunkPath = Path().apply {
        moveTo(x - w / 1.5f, y)
        cubicTo(x - w, y - h * 0.3f, x - w * 0.5f, y - h * 0.7f, x - w * 0.3f, y - h)
        lineTo(x + w * 0.3f, y - h)
        cubicTo(x + w * 0.5f, y - h * 0.7f, x + w, y - h * 0.3f, x + w / 1.5f, y)
        close()
    }
    
    // Dark brown textured bark
    drawPath(
        path = trunkPath,
        brush = Brush.linearGradient(
            0f to Color(0xFF1A120B),
            0.5f to Color(0xFF3C2A21),
            1f to Color(0xFF1A120B),
            start = Offset(x - w, y),
            end = Offset(x + w, y)
        )
    )
    
    // Bark texture lines
    repeat(5) { i ->
        val tx = x - w/2f + (i * w/4f)
        drawLine(
            color = Color.Black.copy(alpha = 0.2f),
            start = Offset(tx, y),
            end = Offset(tx + (i-2)*5f, y - h),
            strokeWidth = 2f
        )
    }
}

fun DrawScope.drawVisibleRoots(x: Float, y: Float, w: Float, progress: Float) {
    repeat(3) { i ->
        val rx = x + (i - 1) * w * 1.2f
        drawPath(
            path = Path().apply {
                moveTo(x + (i - 1) * w * 0.5f, y)
                quadraticTo(rx, y + 20f, rx + (i - 1) * 30f, y + 40f)
            },
            color = Color(0xFF1A120B),
            style = Stroke(width = 8f * progress, cap = StrokeCap.Round)
        )
    }
}

fun DrawScope.drawAncientCanopy(start: Offset, progress: Float, sway: Float, canvasW: Float) {
    val random = java.util.Random(99)
    
    // Massive branching system (Iterative)
    repeat(6) { i ->
        val angle = -70f + (i * 28f) + sway * 2f
        val bLen = (canvasW * 0.35f) * progress
        val rad = Math.toRadians(angle.toDouble() - 90.0)
        val end = Offset(start.x + cos(rad).toFloat() * bLen, start.y + sin(rad).toFloat() * bLen)

        // Large Primary Branches
        drawLine(
            color = Color(0xFF2D2424),
            start = start,
            end = end,
            strokeWidth = 12f * progress,
            cap = StrokeCap.Round
        )

        // Secondary Branches
        repeat(3) { j ->
            val sAngle = angle + (j - 1) * 30f
            val sLen = bLen * 0.5f
            val sRad = Math.toRadians(sAngle.toDouble() - 90.0)
            val sEnd = Offset(end.x + cos(sRad).toFloat() * sLen, end.y + sin(sRad).toFloat() * sLen)
            
            drawLine(
                color = Color(0xFF2D2424),
                start = end,
                end = sEnd,
                strokeWidth = 6f * progress,
                cap = StrokeCap.Round
            )

            // Dense Foliage Clusters
            if (progress > 0.4f) {
                drawDenseLeafCluster(sEnd, progress, random)
            }
        }
    }
}

fun DrawScope.drawDenseLeafCluster(center: Offset, progress: Float, random: java.util.Random) {
    repeat(25) { // Hundreds of leaves overall
        val offX = (random.nextFloat() - 0.5f) * 120f * progress
        val offY = (random.nextFloat() - 0.5f) * 120f * progress
        val leafSize = (16f + random.nextFloat() * 12f) * progress
        val leafRot = random.nextFloat() * 360f
        
        withTransform({
            rotate(leafRot, pivot = Offset(center.x + offX, center.y + offY))
        }) {
            drawMajesticLeaf(Offset(center.x + offX, center.y + offY), leafSize, progress)
        }
    }
}

fun DrawScope.drawMajesticLeaf(pos: Offset, size: Float, alpha: Float) {
    val leafPath = Path().apply {
        moveTo(pos.x, pos.y - size / 2f)
        quadraticTo(pos.x + size / 2.2f, pos.y, pos.x, pos.y + size / 2f)
        quadraticTo(pos.x - size / 2.2f, pos.y, pos.x, pos.y - size / 2f)
    }
    
    drawPath(
        path = leafPath,
        brush = Brush.verticalGradient(
            listOf(Color(0xFF1B5E20).copy(alpha = alpha), Color(0xFF00C853).copy(alpha = alpha))
        )
    )
}

fun DrawScope.drawAtmosphericParticles(x: Float, y: Float, progress: Float) {
    if (progress < 0.3f) return
    val t = System.currentTimeMillis() / 2500f
    repeat(15) { i ->
        val px = x + sin(t + i) * 180f
        val py = y + cos(t * 0.6f + i) * 150f
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = 1.2f,
            center = Offset(px, py)
        )
    }
}

fun getAncientStageName(days: Int): String {
    return when {
        days < 10 -> "Sleeping Seed"
        days < 30 -> "Emerald Sprout"
        days < 60 -> "Strong Sentinel"
        days < 80 -> "Ancient Wisdom Tree"
        else -> "Eternal World Tree"
    }
}
