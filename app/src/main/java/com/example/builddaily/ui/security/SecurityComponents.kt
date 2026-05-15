package com.example.builddaily.ui.security

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmbientGlow() {
    val transition = rememberInfiniteTransition()
    val glowY by transition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.15f), Color.Transparent),
                center = center.copy(y = center.y + glowY)
            ),
            radius = size.width
        )
    }
}

@Composable
fun PinKeypad(
    length: Int,
    currentInput: String,
    error: String?,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val availableWidth = maxWidth
        val isTablet = availableWidth > 600.dp
        val keySize = if (isTablet) 80.dp else 64.dp
        val spacing = if (isTablet) 24.dp else 16.dp
        val gridWidth = (keySize * 3) + (spacing * 2)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = if (isTablet) 56.dp else 40.dp)
            ) {
                repeat(length) { i ->
                    val isFilled = i < currentInput.length
                    Box(
                        modifier = Modifier
                            .size(if (isTablet) 18.dp else 14.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) Color(0xFF8B5CF6) 
                                else Color.White.copy(alpha = 0.1f)
                            )
                    )
                }
            }

            if (error != null) {
                Text(
                    error, 
                    color = Color.Red.copy(alpha = 0.8f), 
                    fontSize = if (isTablet) 14.sp else 12.sp, 
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Numbers Grid
            val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
            Column(
                modifier = Modifier.width(gridWidth),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                numbers.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        row.forEach { num ->
                            if (num.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(keySize)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .clickable { 
                                            if (num == "DEL") onDeleteClick() 
                                            else if (currentInput.length < length) onNumberClick(num) 
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        num, 
                                        color = Color.White, 
                                        fontSize = if (isTablet) 28.sp else 22.sp, 
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(keySize))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordInput(
    input: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    buttonText: String = "Unlock"
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = input,
            onValueChange = onInputChange,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            placeholder = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Enter password", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White.copy(alpha = 0.3f))
                }
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color(0xFF8B5CF6),
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LockoutOverlay(seconds: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Too Many Attempts", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Please wait $seconds seconds before retrying",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.1f)
    )
    val glowAlpha by animateFloatAsState(if (checked) 0.5f else 0f)

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Glow effect for active state
        if (checked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(8.dp)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.3f))
            )
        }
        
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
