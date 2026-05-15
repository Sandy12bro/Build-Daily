package com.example.builddaily.ui.security

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val screenHeight = maxHeight
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(if (screenHeight > 600.dp) 40.dp else 20.dp))

                    // App Logo / Lock Icon
                    Box(
                        modifier = Modifier
                            .size(if (screenHeight > 600.dp) 80.dp else 60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(if (screenHeight > 600.dp) 32.dp else 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Access Secure Vault",
                        color = Color.White,
                        fontSize = if (screenHeight > 600.dp) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        "Verify your identity to continue",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = if (screenHeight > 600.dp) 14.sp else 12.sp
                    )

                    Spacer(modifier = Modifier.height(if (screenHeight > 600.dp) 48.dp else 24.dp))

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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    PatternLockView(
                                        modifier = Modifier.sizeIn(maxWidth = 300.dp, maxHeight = 300.dp),
                                        onPatternComplete = { viewModel.onPatternComplete(it.joinToString(",")) }
                                    )
                                    if (state.error != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(state.error!!, color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp)
                                    }
                                }
                            }
                            LockType.PASSWORD -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    PasswordInput(
                                        input = state.input,
                                        onInputChange = viewModel::onInputChanged,
                                        onSubmit = viewModel::onPasswordSubmit
                                    )
                                    if (state.error != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(state.error!!, color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp)
                                    }
                                }
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
            }
        }

        // Lockout Overlay
        if (state.isLockedOut) {
            LockoutOverlay(state.lockoutSecondsRemaining)
        }
    }
}
