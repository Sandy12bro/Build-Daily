package com.example.builddaily.ui.pomodoro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.builddaily.ui.components.AppTitleWithLogo
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.MintGreen
import com.example.builddaily.ui.theme.CyberPurple
import kotlin.math.floor

enum class PomodoroMode(val label: String, val color: Color) {
    POMODORO("Focus", ElectricBlue),
    SHORT_BREAK("Break", MintGreen),
    LONG_BREAK("Long Break", CyberPurple)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    viewModel: PomodoroViewModel = viewModel()
) {
    val mode by viewModel.mode.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val focusDuration by viewModel.focusDuration.collectAsState()
    val shortBreakDuration by viewModel.shortBreakDuration.collectAsState()
    val longBreakDuration by viewModel.longBreakDuration.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }

    // Flash effect for completion
    val infiniteTransition = rememberInfiniteTransition(label = "CompletedPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
        label = "Pulse"
    )
    
    val flashColor by infiniteTransition.animateColor(
        initialValue = MintGreen,
        targetValue = Color.White,
        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
        label = "Flash"
    )

    val currentDurationMinutes = when(mode) {
        PomodoroMode.POMODORO -> focusDuration
        PomodoroMode.SHORT_BREAK -> shortBreakDuration
        PomodoroMode.LONG_BREAK -> longBreakDuration
    }
    
    val totalTimeSeconds = currentDurationMinutes * 60
    val progress = if (totalTimeSeconds > 0) timeLeft.toFloat() / totalTimeSeconds else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "TimerProgress"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { AppTitleWithLogo("Pomodoro", showLogo = false) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        val sessions by viewModel.sessions.collectAsState()
        val stats by viewModel.stats.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PomodoroMode.values().forEach { m ->
                    val isSelected = mode == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) m.color.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) m.color.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .alpha(if (isRunning && !isSelected) 0.3f else 1f)
                            .clickable(enabled = !isRunning) { viewModel.setMode(m) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = m.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) m.color else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Timer Display
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = mode.color,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    if (timeLeft == 0) {
                        drawArc(
                            color = MintGreen.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(if (timeLeft == 0) pulseScale else 1f)
                ) {
                    val minutes = floor(timeLeft / 60f).toInt()
                    val seconds = timeLeft % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (timeLeft == 0) flashColor else Color.White,
                        fontSize = 64.sp
                    )
                    Text(
                        text = if (isRunning) "FOCUSING" else if (timeLeft == 0) "WELL DONE!" else "READY",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (timeLeft == 0) MintGreen else mode.color.copy(alpha = 0.6f),
                        letterSpacing = 4.sp,
                        fontWeight = if (timeLeft == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White.copy(alpha = 0.6f))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(mode.color)
                        .clickable { viewModel.toggleTimer(context) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isRunning) "PAUSE" else if (timeLeft == 0) "RESTART" else "START FOCUS",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Stats Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBadge(
                    label = "Day streak",
                    value = "${stats.dayStreak}",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFF8B9FED), // Light blue from image
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Total focus",
                    value = formatFocusTime(stats.totalFocusMinutes),
                    icon = Icons.Default.WorkspacePremium,
                    color = Color(0xFFB5B5B5), // Grey from image
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // History Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue,
                    modifier = Modifier.clickable { onShowHistory() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sessions yet", color = Color.White.copy(alpha = 0.3f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sessions.forEach { session ->
                        SessionItem(session)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showSettings) {
        PomodoroSettingsDialog(
            focus = focusDuration,
            short = shortBreakDuration,
            long = longBreakDuration,
            onDismiss = { showSettings = false },
            onSave = { f, s, l ->
                viewModel.setDurations(f, s, l)
                showSettings = false
            }
        )
    }

    if (isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompletion() },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("🏆 Session Complete!", color = MintGreen, fontWeight = FontWeight.Bold) },
            text = { Text("Your ${mode.label} period has ended. Time for a well-deserved reset!", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCompletion() },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                ) {
                    Text("Got it!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun formatFocusTime(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
fun StatBadge(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SessionItem(session: com.example.builddaily.data.model.PomodoroSession) {
    val modeColor = when(session.mode) {
        "Focus" -> ElectricBlue
        "Break" -> MintGreen
        "Long Break" -> CyberPurple
        else -> ElectricBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(modeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = session.mode.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = modeColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Session Info (Middle - taking remaining space)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${session.mode} session",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = java.time.LocalDate.parse(session.date).format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            // Duration Badge (Stylized Star/Flower)
            Box(
                modifier = Modifier
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background shape (Multiple rotated squares to make a star/flower)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .graphicsLayer(rotationZ = 45f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(modeColor.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .graphicsLayer(rotationZ = 22.5f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(modeColor.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(modeColor.copy(alpha = 0.1f))
                )
                
                Text(
                    text = "${session.durationMinutes}m",
                    style = MaterialTheme.typography.titleMedium,
                    color = modeColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun PomodoroSettingsDialog(
    focus: Int,
    short: Int,
    long: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int) -> Unit
) {
    var f by remember { mutableStateOf(focus.toFloat()) }
    var s by remember { mutableStateOf(short.toFloat()) }
    var l by remember { mutableStateOf(long.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Customize Timer", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SettingsSlider(label = "Focus Time", value = f, range = 1f..60f, color = ElectricBlue) { f = it }
                SettingsSlider(label = "Short Break", value = s, range = 1f..15f, color = MintGreen) { s = it }
                SettingsSlider(label = "Long Break", value = l, range = 1f..30f, color = CyberPurple) { l = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(f.toInt(), s.toInt(), l.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
fun SettingsSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, color: Color, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            Text("${value.toInt()} min", color = color, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}
