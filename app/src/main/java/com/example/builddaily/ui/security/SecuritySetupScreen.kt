package com.example.builddaily.ui.security

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.security.LockType
import com.example.builddaily.ui.components.PatternLockView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SecuritySetupScreen(
    viewModel: SecurityViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0118))
    ) {
        AmbientGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            SetupHeader(state.setupStep, onBack)

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = state.setupStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() with slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }
            ) { step ->
                when (step) {
                    SetupStep.VERIFY_OLD -> EntryStep(
                        type = settings.lockType,
                        input = state.input,
                        error = state.error,
                        onInputChanged = viewModel::onInputChanged,
                        onPatternComplete = viewModel::onPatternComplete,
                        onDelete = { if (state.input.isNotEmpty()) viewModel.onInputChanged(state.input.dropLast(1)) },
                        onSubmit = viewModel::submitSetupInput,
                        isConfirm = false
                    )
                    SetupStep.CHOOSE_METHOD -> ChooseMethodStep(
                        selected = state.selectedLockType,
                        onSelect = viewModel::selectLockType,
                        onContinue = viewModel::startSetup
                    )
                    SetupStep.ENTER_SECRET -> EntryStep(
                        type = state.selectedLockType ?: LockType.PIN_4,
                        input = state.input,
                        error = state.error,
                        strength = state.passwordStrength,
                        strengthLabel = state.passwordStrengthLabel,
                        onInputChanged = viewModel::onInputChanged,
                        onPatternComplete = viewModel::onPatternComplete,
                        onDelete = { if (state.input.isNotEmpty()) viewModel.onInputChanged(state.input.dropLast(1)) },
                        onSubmit = viewModel::submitSetupInput,
                        isConfirm = false
                    )
                    SetupStep.CONFIRM_SECRET -> EntryStep(
                        type = state.selectedLockType ?: LockType.PIN_4,
                        input = state.input,
                        error = state.error,
                        onInputChanged = viewModel::onInputChanged,
                        onPatternComplete = viewModel::onPatternComplete,
                        onDelete = { if (state.input.isNotEmpty()) viewModel.onInputChanged(state.input.dropLast(1)) },
                        onSubmit = viewModel::submitSetupInput,
                        isConfirm = true
                    )
                    SetupStep.SUCCESS -> SuccessStep(onComplete)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SetupHeader(step: SetupStep, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = when (step) {
                    SetupStep.VERIFY_OLD -> "Verify Identity"
                    SetupStep.CHOOSE_METHOD -> "Security Method"
                    SetupStep.ENTER_SECRET -> "Create Lock"
                    SetupStep.CONFIRM_SECRET -> "Confirm Lock"
                    SetupStep.SUCCESS -> "All Set!"
                },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (step) {
                    SetupStep.VERIFY_OLD -> "Confirm your current lock to change it"
                    SetupStep.CHOOSE_METHOD -> "Choose how to protect your vault"
                    SetupStep.ENTER_SECRET -> "Set your secure access credential"
                    SetupStep.CONFIRM_SECRET -> "Verify your new security lock"
                    SetupStep.SUCCESS -> "Your vault is now protected"
                },
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ChooseMethodStep(
    selected: LockType?,
    onSelect: (LockType) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mini Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(targetState = selected) { type ->
                when (type) {
                    LockType.PIN_4, LockType.PIN_6 -> PinPreview(if (type == LockType.PIN_4) 4 else 6)
                    LockType.PATTERN -> PatternPreview()
                    LockType.PASSWORD -> PasswordPreview()
                    LockType.NONE, null -> Text("Select a method below", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                }
            }
        }

        val methods = listOf(
            MethodItem(LockType.PIN_4, Icons.Default.Dialpad, "4-Digit PIN", "Fast and simple protection"),
            MethodItem(LockType.PIN_6, Icons.Default.Dialpad, "6-Digit PIN", "Stronger numeric security"),
            MethodItem(LockType.PATTERN, Icons.Default.Gesture, "Pattern Lock", "Quick gesture-based unlock"),
            MethodItem(LockType.PASSWORD, Icons.Default.Password, "Password Lock", "Maximum security with letters & numbers")
        )

        methods.forEach { method ->
            SecurityMethodCard(
                method = method,
                isSelected = selected == method.type,
                onClick = { onSelect(method.type) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6),
                disabledContainerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PinPreview(length: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(length) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF8B5CF6).copy(alpha = 0.5f)))
        }
    }
}

@Composable
fun PatternPreview() {
    Icon(Icons.Default.Gesture, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(40.dp))
}

@Composable
fun PasswordPreview() {
    Icon(Icons.Default.Password, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(40.dp))
}

data class MethodItem(val type: LockType, val icon: ImageVector, val title: String, val desc: String)

@Composable
fun SecurityMethodCard(method: MethodItem, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.02f else 1f)
    val borderAlpha by animateFloatAsState(if (isSelected) 0.8f else 0.1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = borderAlpha))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = if (isSelected) 0.2f else 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(method.icon, contentDescription = null, tint = if (isSelected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(method.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(method.desc, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF8B5CF6))
            }
        }
    }
}

@Composable
fun EntryStep(
    type: LockType,
    input: String,
    error: String?,
    strength: Float = 0f,
    strengthLabel: String = "",
    onInputChanged: (String) -> Unit,
    onPatternComplete: (String) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    isConfirm: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (type) {
            LockType.PIN_4, LockType.PIN_6 -> {
                PinKeypad(
                    length = if (type == LockType.PIN_4) 4 else 6,
                    currentInput = input,
                    error = error,
                    onNumberClick = { onInputChanged(input + it) },
                    onDeleteClick = onDelete
                )
            }
            LockType.PATTERN -> {
                Text(if (isConfirm) "Redraw to confirm" else "Draw your pattern", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 24.dp))
                PatternLockView(
                    onPatternComplete = { onPatternComplete(it.joinToString(",")) }
                )
                if (error != null) {
                    Text(error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                }
            }
            LockType.PASSWORD -> {
                PasswordInputStep(
                    input = input,
                    onInputChange = onInputChanged,
                    onSubmit = onSubmit,
                    strength = strength,
                    strengthLabel = strengthLabel,
                    error = error,
                    isConfirm = isConfirm
                )
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputStep(
    input: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    strength: Float,
    strengthLabel: String,
    error: String?,
    isConfirm: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = input,
            onValueChange = onInputChange,
            placeholder = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Enter password", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF8B5CF6)
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isConfirm) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Strength: $strengthLabel", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            LinearProgressIndicator(
                progress = strength,
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = when {
                    strength < 0.5f -> Color.Red
                    strength < 0.8f -> Color.Yellow
                    else -> Color.Green
                },
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }

        if (error != null) {
            Text(error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isConfirm) "Confirm" else "Next", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SuccessStep(onComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.1f))
                .border(2.dp, Color(0xFF8B5CF6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(64.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Setup Complete", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Your vault is now secure", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Finish", fontWeight = FontWeight.Bold)
        }
    }
}
