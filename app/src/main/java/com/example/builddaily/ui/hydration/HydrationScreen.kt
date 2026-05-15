package com.example.builddaily.ui.hydration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.builddaily.ui.components.AppTitleWithLogo
import com.example.builddaily.ui.theme.*
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import com.example.builddaily.util.formatTime
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    onBack: () -> Unit,
    statsRepository: com.example.builddaily.data.repository.UserStatsRepository
) {
    val context = LocalContext.current
    val repository = remember { HydrationRepository(context) }
    // Initialize custom factory for HydrationViewModel
    val viewModel: HydrationViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HydrationViewModel(repository, statsRepository) as T
            }
        }
    )

    val config by viewModel.config.collectAsState()
    val records by viewModel.records.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val consumedTodayMl by viewModel.consumedTodayMl.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val aggregatedMl by viewModel.aggregatedMl.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var showCustomDrinkDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDrinkTypeForCustom by remember { mutableStateOf("Water") }

    // Premium UI Theme colors
    val CyanAccent = Color(0xFF06B6D4)
    val GlowBlue = Color(0xFF38BDF8)
    val GlassSurface = Color.White.copy(alpha = 0.06f)

    val selectedDateStr = selectedDate.toString()
    val filteredRecords = records.filter { it.dateStr == selectedDateStr }

    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        NebulaBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { AppTitleWithLogo("Hydration", showLogo = false) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showConfigDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Calculate Goal", tint = CyanAccent)
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // 1. Daily Goal Card
                item {
                    DailyGoalCard(
                        config = config,
                        onOpenCalculator = { showConfigDialog = true }
                    )
                }

                // 2. Animated Water Progress Visual
                item {
                    AnimatedWaterProgressSection(
                        consumedMl = consumedTodayMl,
                        goalMl = config.calculatedGoalMl,
                        cyanAccent = CyanAccent,
                        glowBlue = GlowBlue
                    )
                }

                // 3. Quick Add Water Buttons
                item {
                    QuickAddSection(
                        onAddMl = { amount -> viewModel.addWaterIntake(amount) },
                        onCustomMl = {
                            selectedDrinkTypeForCustom = "Water"
                            showCustomDrinkDialog = true
                        },
                        cyanAccent = CyanAccent
                    )
                }

                // useful Extra Feature: Caffeine Track Option
                item {
                    CaffeineTrackOptionCard(
                        onAddCaffeine = {
                            selectedDrinkTypeForCustom = "Caffeine"
                            showCustomDrinkDialog = true
                        }
                    )
                }

                // 4. Smart Notifications Trigger / Testing center
                item {
                    SmartNotificationsPanel(config = config, onUpdateConfig = { viewModel.updateConfig(it) })
                }

                // 5. History & Insights Navigator
                item {
                    HistoryNavigatorCard(
                        selectedDate = selectedDate,
                        viewMode = viewMode,
                        aggregatedMl = aggregatedMl,
                        onModeChange = { viewModel.setViewMode(it) },
                        onOpenDatePicker = { showDatePicker = true },
                        cyanAccent = CyanAccent
                    )
                }

                // 6. Wellness Indicators & Tips
                item {
                    WellnessIndicatorsSection(consumedMl = consumedTodayMl, goalMl = config.calculatedGoalMl)
                }

                // 6. Analytics Overview
                item {
                    AnalyticsCard(consumedMl = consumedTodayMl, goalMl = config.calculatedGoalMl, stats = stats)
                }

                // 7. Streak System & Achievements
                item {
                    AchievementsGrid(stats = stats)
                }

                // 8. Hydration Timeline Log
                item {
                    Text(
                        text = if (selectedDate.toString() == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()) "Today's Intake Log" else "Intake Log for $selectedDate",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                if (filteredRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hydration tracked yet today.",
                                color = Color.White.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(filteredRecords, key = { it.id }) { record ->
                        HydrationLogItem(
                            record = record,
                            onDelete = { viewModel.deleteRecord(record.id) }
                        )
                    }
                }
            }
        }

        // Custom Calculator Modal Dialog
        if (showConfigDialog) {
            GoalCalculatorDialog(
                currentConfig = config,
                onDismiss = { showConfigDialog = false },
                onSave = { updated ->
                    viewModel.updateConfig(updated)
                    showConfigDialog = false
                    ActionMessageManager.postMessage("Daily Intake Goal Calculated!", ActionType.UPDATED)
                }
            )
        }

        // Custom Amount Prompt
        if (showCustomDrinkDialog) {
            CustomAmountDialog(
                drinkType = selectedDrinkTypeForCustom,
                onDismiss = { showCustomDrinkDialog = false },
                onSubmit = { ml ->
                    if (selectedDrinkTypeForCustom == "Water") {
                        viewModel.addWaterIntake(ml)
                    } else {
                        viewModel.addCustomDrink(ml, selectedDrinkTypeForCustom)
                    }
                    showCustomDrinkDialog = false
                }
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { ms ->
                            val instant = Instant.fromEpochMilliseconds(ms)
                            val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            viewModel.setSelectedDate(date)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = CyanAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryNavigatorCard(
    selectedDate: LocalDate,
    viewMode: HydrationViewMode,
    aggregatedMl: Int,
    onModeChange: (HydrationViewMode) -> Unit,
    onOpenDatePicker: () -> Unit,
    cyanAccent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "History & Insights",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenDatePicker() }
                    ) {
                        Text(
                            text = when(viewMode) {
                                HydrationViewMode.DAY -> selectedDate.toString()
                                HydrationViewMode.WEEK -> "Week of ${selectedDate}"
                                HydrationViewMode.MONTH -> "${selectedDate.month} ${selectedDate.year}"
                                HydrationViewMode.YEAR -> "${selectedDate.year}"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Intake",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "$aggregatedMl ml",
                        style = MaterialTheme.typography.titleMedium,
                        color = cyanAccent,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // View Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HydrationViewMode.entries.forEach { mode ->
                    val isSelected = viewMode == mode
                    Button(
                        onClick = { onModeChange(mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) cyanAccent else Color.White.copy(alpha = 0.05f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) SpaceBlack else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyGoalCard(config: HydrationGoalConfig, onOpenCalculator: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calculated Daily Target",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${config.calculatedGoalMl} ml",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF06B6D4),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onOpenCalculator,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4).copy(alpha = 0.15f)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recalibrate", color = Color(0xFF06B6D4), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Formula detail readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormulaBadge("${config.weightKg}kg")
                FormulaBadge(config.gender)
                FormulaBadge("Activity: ${config.activityLevel}")
                FormulaBadge("Temp: ${config.weatherTemp}")
            }
        }
    }
}

@Composable
fun FormulaBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AnimatedWaterProgressSection(consumedMl: Int, goalMl: Int, cyanAccent: Color, glowBlue: Color) {
    val percentage = if (goalMl > 0) (consumedMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
    val remaining = (goalMl - consumedMl).coerceAtLeast(0)

    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "WaterFillAnim"
    )

    // Pulsing glow factor
    val infiniteTransition = rememberInfiniteTransition(label = "WaterPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse),
        label = "Pulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, cyanAccent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Foreground animated cyan water fill arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(cyanAccent, glowBlue, cyanAccent)
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedPercentage,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw inner simulated fluid/droplet icon space
                    val centerOffset = Offset(size.width / 2, size.height / 2)
                    drawCircle(
                        color = cyanAccent.copy(alpha = 0.05f * pulseRadius),
                        radius = (size.width / 2.5f) * pulseRadius,
                        center = centerOffset
                    )
                }

                // Inner dynamic text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = cyanAccent.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp).padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${(animatedPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$consumedMl / $goalMl ml",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Consumed", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Text("$consumedMl ml", style = MaterialTheme.typography.titleMedium, color = cyanAccent, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Text("$remaining ml", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickAddSection(onAddMl: (Int) -> Unit, onCustomMl: () -> Unit, cyanAccent: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Log Intake",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CupButton(title = "100 ml", subtitle = "Sip", cyanAccent = cyanAccent, modifier = Modifier.weight(1f)) { onAddMl(100) }
            CupButton(title = "250 ml", subtitle = "Glass", cyanAccent = cyanAccent, modifier = Modifier.weight(1f)) { onAddMl(250) }
            CupButton(title = "500 ml", subtitle = "Bottle", cyanAccent = cyanAccent, modifier = Modifier.weight(1f)) { onAddMl(500) }
            CupButton(title = "Custom", subtitle = "Input", cyanAccent = Color.White, modifier = Modifier.weight(1f), isCustom = true) { onCustomMl() }
        }
    }
}

@Composable
fun CupButton(title: String, subtitle: String, cyanAccent: Color, modifier: Modifier = Modifier, isCustom: Boolean = false, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(75.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCustom) Color.White.copy(alpha = 0.08f) else cyanAccent.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, if (isCustom) Color.White.copy(alpha = 0.2f) else cyanAccent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                if (isCustom) Icons.Default.Add else Icons.Default.LocalDrink,
                contentDescription = null,
                tint = if (isCustom) Color.White else cyanAccent,
                modifier = Modifier.size(20.dp).padding(bottom = 2.dp)
            )
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }
}

@Composable
fun CaffeineTrackOptionCard(onAddCaffeine: () -> Unit) {
    Card(
        onClick = onAddCaffeine,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SolarYellow.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, SolarYellow.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(SolarYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Coffee, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Caffeine Track", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Log coffee/tea to view dehydration balance", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = SolarYellow)
        }
    }
}

@Composable
fun SmartNotificationsPanel(config: HydrationGoalConfig, onUpdateConfig: (HydrationGoalConfig) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        val context = LocalContext.current
        Column(modifier = Modifier.padding(20.dp)) {
            val nextAlarm = remember(config) { com.example.builddaily.util.HydrationScheduler.getNextAlarmTime(context) }
            val nextAlarmStr = remember(nextAlarm) {
                if (nextAlarm == 0L) "Not scheduled"
                else {
                    val cal = Calendar.getInstance().apply { timeInMillis = nextAlarm }
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Intelligent Reminders", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06B6D4).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Next: $nextAlarmStr",
                        color = Color(0xFF06B6D4),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Reminder Interval", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(6.dp))
            
            // Interval selector row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 90).forEach { mins ->
                    val selected = config.reminderIntervalMins == mins
                    Button(
                        onClick = { onUpdateConfig(config.copy(reminderIntervalMins = mins)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("$mins m", color = if (selected) SpaceBlack else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quiet Hours info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Quiet Hours (Sleep state)", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Text("${config.quietHoursStart} - ${config.quietHoursEnd} • Reminders paused", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                }
                
                Switch(
                    checked = config.bedtimeReduction,
                    onCheckedChange = { onUpdateConfig(config.copy(bedtimeReduction = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF06B6D4), checkedTrackColor = Color(0xFF06B6D4).copy(alpha = 0.4f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Notification testing trigger
            val testContext = LocalContext.current
            Button(
                onClick = {
                    val messages = listOf(
                        "Time to hydrate! Keep your wellness peak high 💧",
                        "You haven't had water for ${config.reminderIntervalMins} mins. Drink up!",
                        "Hydration improves brain memory and speed ✨",
                        "Stay refreshed. Build your life architecture fully."
                    )
                    val msg = messages.random()
                    // 1. Show UI toast
                    ActionMessageManager.postMessage("Firing test notification...", ActionType.ADDED)
                    // 2. Fire real system notification
                    val helper = com.example.builddaily.util.NotificationHelper(testContext)
                    helper.showNotification("Hydration Mission", msg)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Test System Notification", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun WellnessIndicatorsSection(consumedMl: Int, goalMl: Int) {
    val energyFactor = if (goalMl > 0) ((consumedMl.toFloat() / goalMl) * 100).toInt().coerceIn(40, 100) else 50
    val stateLabel = when {
        energyFactor >= 90 -> "Optimal Cellular Vibe"
        energyFactor >= 70 -> "Refreshed & Focused"
        energyFactor >= 50 -> "Slight Dehydration"
        else -> "Drink Alert Active"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Body State Meter", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Hydration Energy Factor: $energyFactor%", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(stateLabel, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hydration Tip Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF06B6D4).copy(alpha = 0.08f))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Wellness Insight: Drinking water early morning jumpstarts your brain synapses and accelerates mission productivity.",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(consumedMl: Int, goalMl: Int, stats: HydrationStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Premium Insights", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AnalyticsDetailColumn("Today's Efficiency", if (goalMl > 0) "${((consumedMl.toFloat() / goalMl) * 100).toInt()}%" else "0%")
                AnalyticsDetailColumn("Lifetime Fluid", "${stats.totalConsumedVolumeDisplay}")
                AnalyticsDetailColumn("Daily Consistency", "94% Avg")
            }
        }
    }
}

@Composable
fun AnalyticsDetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
    }
}

val HydrationStats.totalConsumedVolumeDisplay: String
    get() {
        val l = totalConsumedLifetimeMl / 1000f
        return if (l >= 1f) String.format("%.1f L", l) else "$totalConsumedLifetimeMl ml"
    }

@Composable
fun AchievementsGrid(stats: HydrationStats) {
    val standardBadges = listOf(
        "Perfect Hydration Day" to "Reach 100% daily target",
        "7 Day Hydration Streak" to "Maintain water streak for 7 days",
        "Hydration Initiate" to "Track for 3 days consistently",
        "Hydration Master" to "Consume over 25L total lifetime"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Streaks & Trophies",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Active Daily Streak: ${stats.streakDays} Days", color = SolarYellow, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }

        // Horizontal showcase of premium badges
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(standardBadges) { (title, subtitle) ->
                val isUnlocked = stats.unlockedBadges.contains(title)
                BadgeItemCard(title = title, subtitle = subtitle, isUnlocked = isUnlocked)
            }
        }
    }
}

@Composable
fun BadgeItemCard(title: String, subtitle: String, isUnlocked: Boolean) {
    val borderColor = if (isUnlocked) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.1f)
    val bgColor = if (isUnlocked) Color(0xFF06B6D4).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f)

    Card(
        modifier = Modifier.width(160.dp).height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isUnlocked) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(16.dp)
                )
                if (isUnlocked) {
                    Text("UNLOCKED", color = Color(0xFF06B6D4), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column {
                Text(title, color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                Text(subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, maxLines = 2, lineHeight = 11.sp)
            }
        }
    }
}

@Composable
fun HydrationLogItem(record: HydrationRecord, onDelete: () -> Unit) {
    val isCaffeine = record.drinkType == "Caffeine"
    val color = if (isCaffeine) SolarYellow else Color(0xFF06B6D4)

    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCaffeine) Icons.Default.Coffee else Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(record.drinkType, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    val timeStr = remember(record.timestamp) {
                        val c = Calendar.getInstance()
                        c.timeInMillis = record.timestamp
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(c.time)
                    }
                    Text(timeStr, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("+${record.amountMl} ml", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun GoalCalculatorDialog(currentConfig: HydrationGoalConfig, onDismiss: () -> Unit, onSave: (HydrationGoalConfig) -> Unit) {
    var weight by remember { mutableStateOf(currentConfig.weightKg.toString()) }
    var gender by remember { mutableStateOf(currentConfig.gender) }
    var activity by remember { mutableStateOf(currentConfig.activityLevel) }
    var temp by remember { mutableStateOf(currentConfig.weatherTemp) }
    var customGoalMlStr by remember { mutableStateOf(currentConfig.customGoalMl?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        titleContentColor = Color.White,
        title = { Text("Smart Target Calculator", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Body Weight (kg)", color = Color.White.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender Segment
                Column {
                    Text("Gender", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            val sel = gender == g
                            Button(
                                onClick = { gender = g },
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(g, color = if (sel) SpaceBlack else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Activity Level
                Column {
                    Text("Activity Intensity", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Moderate", "High").forEach { a ->
                            val sel = activity == a
                            Button(
                                onClick = { activity = a },
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(a, color = if (sel) SpaceBlack else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Weather Temp
                Column {
                    Text("Climate Heat", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cool", "Normal", "Hot").forEach { t ->
                            val sel = temp == t
                            Button(
                                onClick = { temp = t },
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(t, color = if (sel) SpaceBlack else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = customGoalMlStr,
                    onValueChange = { customGoalMlStr = it },
                    label = { Text("Override Target ml (Optional)", color = Color.White.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weight.toIntOrNull() ?: 70
                    val overrideMl = customGoalMlStr.toIntOrNull()
                    onSave(
                        currentConfig.copy(
                            weightKg = w,
                            gender = gender,
                            activityLevel = activity,
                            weatherTemp = temp,
                            customGoalMl = overrideMl
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
            ) {
                Text("Confirm", color = SpaceBlack, fontWeight = FontWeight.Bold)
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
fun CustomAmountDialog(drinkType: String, onDismiss: () -> Unit, onSubmit: (Int) -> Unit) {
    var inputStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        titleContentColor = Color.White,
        title = { Text("Log Custom $drinkType", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = inputStr,
                onValueChange = { inputStr = it },
                label = { Text("Volume (ml)", color = Color.White.copy(alpha = 0.6f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF06B6D4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val ml = inputStr.toIntOrNull() ?: 200
                    onSubmit(ml)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
            ) {
                Text("Add Fluid", color = SpaceBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}
