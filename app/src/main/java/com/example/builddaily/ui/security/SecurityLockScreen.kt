package com.example.builddaily.ui.security

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.builddaily.data.security.LockType
import com.example.builddaily.ui.components.PatternLockView
import com.example.builddaily.util.BiometricHelper

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SecurityLockScreen(
    viewModel: SecurityViewModel,
    onAuthenticated: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(state.isUnlocked) {
        if (state.isUnlocked) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0118))
    ) {
        // Ambient Background Glow
        AmbientGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Logo / Lock Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Access Secure Vault",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Verify your identity to continue",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Dynamic Input Area
            AnimatedContent(
                targetState = settings.lockType,
                transitionSpec = { fadeIn() with fadeOut() }
            ) { type ->
                when (type) {
                    LockType.PIN_4, LockType.PIN_6 -> {
                        PinKeypad(
                            length = if (type == LockType.PIN_4) 4 else 6,
                            currentInput = state.input,
                            error = state.error,
                            onNumberClick = { viewModel.onInputChanged(state.input + it) },
                            onDeleteClick = { if (state.input.isNotEmpty()) viewModel.onInputChanged(state.input.dropLast(1)) }
                        )
                    }
                    LockType.PATTERN -> {
                        PatternLockView(
                            onPatternComplete = { viewModel.onInputChanged(it.joinToString(",")) }
                        )
                    }
                    LockType.PASSWORD -> {
                        PasswordInput(
                            input = state.input,
                            onInputChange = viewModel::onInputChanged,
                            onSubmit = viewModel::onPasswordSubmit
                        )
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric Option
            if (settings.isBiometricEnabled) {
                IconButton(
                    onClick = {
                        val helper = BiometricHelper(context)
                        helper.showBiometricPrompt(
                            activity = context as FragmentActivity,
                            onSuccess = { viewModel.onBiometricSuccess() },
                            onError = { /* Handle error */ }
                        )
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Lockout Overlay
        if (state.isLockedOut) {
            LockoutOverlay(state.lockoutSecondsRemaining)
        }
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            repeat(length) { i ->
                val isFilled = i < currentInput.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) Color(0xFF8B5CF6) 
                            else Color.White.copy(alpha = 0.1f)
                        )
                        .border(
                            1.dp, 
                            if (isFilled) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.2f), 
                            CircleShape
                        )
                )
            }
        }

        if (error != null) {
            Text(error, color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Numbers Grid
        val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(numbers) { num ->
                if (num.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable { 
                                if (num == "DEL") onDeleteClick() 
                                else if (currentInput.length < length) onNumberClick(num) 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(num, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInput(
    input: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = input,
            onValueChange = onInputChange,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            Text("Unlock", fontWeight = FontWeight.Bold)
        }
    }
}

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
