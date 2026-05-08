package com.example.builddaily.ui.stats

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import com.example.builddaily.util.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.builddaily.util.toEpochMillis
import com.example.builddaily.util.fromEpochMillis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.MintGreen
import com.example.builddaily.ui.theme.SolarYellow
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    repository: TaskRepository
) {
    val viewModel = remember { StatsViewModel(repository) }
    val period by viewModel.period.collectAsState()
    val referenceDate by viewModel.referenceDate.collectAsState()
    val statsData by viewModel.statsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = referenceDate.toEpochMillis()
    )

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(statsData, period) {
        if (statsData.totalCounts.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(statsData.totalCounts)
                    series(statsData.completedCounts)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { 
                    com.example.builddaily.ui.components.AppTitleWithLogo("Performance") 
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Period Selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    StatsPeriod.values().forEachIndexed { index, p ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsPeriod.values().size),
                            onClick = { viewModel.setPeriod(p) },
                            selected = period == p,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = ElectricBlue,
                                activeContentColor = Color.Black,
                                inactiveContainerColor = Color.White.copy(alpha = 0.05f),
                                inactiveContentColor = Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(p.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Period Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigate(false) },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = ElectricBlue)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Pick Date",
                            tint = ElectricBlue.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statsData.dateRangeText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    val isAtToday = remember(referenceDate, period) {
                        referenceDate >= com.example.builddaily.util.today()
                    }

                    IconButton(
                        onClick = { viewModel.navigate(true) },
                        enabled = !isAtToday,
                        modifier = Modifier
                            .background(
                                if (isAtToday) Color.Transparent else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Forward",
                            tint = if (isAtToday) Color.White.copy(alpha = 0.2f) else ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Grid - Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(label = "TOTAL", value = statsData.overallTotal.toString(), color = CyberPurple, modifier = Modifier.weight(1f))
                    StatCard(label = "DONE", value = statsData.overallCompleted.toString(), color = MintGreen, modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stats Grid - Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val efficiency = if (statsData.overallTotal > 0) (statsData.overallCompleted * 100 / statsData.overallTotal) else 0
                    StatCard(label = "RATE", value = "$efficiency%", color = ElectricBlue, modifier = Modifier.weight(1f))
                    StatCard(label = "STREAK", value = "${statsData.streak}🔥", color = SolarYellow, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Activity Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    CartesianChartHost(
                        modifier = Modifier.fillMaxSize(),
                        modelProducer = modelProducer,
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(
                                lineProvider = LineCartesianLayer.LineProvider.series(
                                    LineCartesianLayer.Line(fill = LineCartesianLayer.LineFill.single(fill(CyberPurple))),
                                    LineCartesianLayer.Line(fill = LineCartesianLayer.LineFill.single(fill(MintGreen)))
                                )
                            ),
                            startAxis = VerticalAxis.rememberStart(
                                itemPlacer = VerticalAxis.ItemPlacer.step({ 1.0 })
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                labelRotationDegrees = if (period == StatsPeriod.DAILY || period == StatsPeriod.MONTHLY) 45f else 0f,
                                valueFormatter = { _, x, _ -> 
                                    statsData.labels.getOrNull(x.toInt()) ?: " "
                                }
                            )
                        ),
                        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
                        zoomState = com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState(zoomEnabled = true)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = CyberPurple, label = "Total Tasks")
                    Spacer(modifier = Modifier.width(24.dp))
                    LegendItem(color = MintGreen, label = "Completed")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Activity Heatmap
                ActivityHeatmap(
                    heatmapData = statsData.heatmapData,
                    period = period,
                    referenceDate = referenceDate
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.setReferenceDate(fromEpochMillis(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", color = ElectricBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(56.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun ActivityHeatmap(
    heatmapData: Map<String, DayActivity>,
    period: StatsPeriod,
    referenceDate: LocalDate
) {
    if (period == StatsPeriod.DAILY) return
    val displayData = remember(period, referenceDate) {
        when (period) {
            StatsPeriod.DAILY -> listOf(referenceDate)
            StatsPeriod.WEEKLY -> {
                val start = referenceDate.minus(referenceDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
                (0..6).map { start.plus(it, DateTimeUnit.DAY) }
            }
            StatsPeriod.MONTHLY -> {
                val start = LocalDate(referenceDate.year, referenceDate.monthNumber, 1)
                val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                (0 until end.dayOfMonth).map { start.plus(it, DateTimeUnit.DAY) }
            }
            StatsPeriod.YEARLY -> {
                val start = LocalDate(referenceDate.year, 1, 1)
                val end = LocalDate(referenceDate.year, 12, 31)
                val days = mutableListOf<LocalDate>()
                var curr = start
                while (curr <= end) {
                    days.add(curr)
                    curr = curr.plus(1, DateTimeUnit.DAY)
                }
                days
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = when (period) {
                StatsPeriod.WEEKLY -> "Weekly Activity"
                StatsPeriod.MONTHLY -> "Monthly Activity"
                StatsPeriod.YEARLY -> "Yearly Activity"
                else -> ""
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            if (period == StatsPeriod.WEEKLY) {
                var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
                val weeklySummary = remember(heatmapData, displayData) {
                    val activeDays = displayData.count { (heatmapData[it.toString()]?.percentage ?: 0f) > 0f }
                    val avgCompletion = if (displayData.isNotEmpty()) {
                        displayData.map { heatmapData[it.toString()]?.percentage ?: 0f }.average() * 100
                    } else 0.0
                    
                    var currentStreak = 0
                    var maxStreak = 0
                    displayData.forEach { date ->
                        if ((heatmapData[date.toString()]?.percentage ?: 0f) >= 0.8f) {
                            currentStreak++; maxStreak = maxOf(maxStreak, currentStreak)
                        } else currentStreak = 0
                    }
                    val topDay = displayData.maxByOrNull { heatmapData[it.toString()]?.percentage ?: 0f }
                    val topDayName = topDay?.dayOfWeek?.name?.take(3)?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None"
                    
                    Quadruple(activeDays, avgCompletion.toInt(), maxStreak, topDayName)
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryItem(label = "Active", value = "${weeklySummary.first}d", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Avg.", value = "${weeklySummary.second}%", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Streak", value = "${weeklySummary.third}🔥", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Top", value = weeklySummary.fourth, modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(text = day, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        displayData.forEach { date ->
                            val activity = heatmapData[date.toString()] ?: DayActivity()
                            val completion = activity.percentage
                            val isSelected = selectedDate == date
                            val color = when {
                                completion >= 1f -> MintGreen
                                completion >= 0.8f -> MintGreen.copy(alpha = 0.8f)
                                completion >= 0.5f -> MintGreen.copy(alpha = 0.5f)
                                completion >= 0.2f -> MintGreen.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                            val scale by animateFloatAsState(if (isSelected) 1.25f else 1f)
                            Box(
                                modifier = Modifier.size(38.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(RoundedCornerShape(10.dp)).background(color)
                                    .border(width = if (isSelected) 2.dp else 0.dp, color = ElectricBlue, shape = RoundedCornerShape(10.dp))
                                    .clickable { selectedDate = if (selectedDate == date) null else date }
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = selectedDate != null, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            selectedDate?.let { date ->
                                val activity = heatmapData[date.toString()] ?: DayActivity()
                                val score = (activity.percentage * 10).toInt()
                                Surface(
                                    modifier = Modifier.padding(top = 16.dp).clip(RoundedCornerShape(10.dp)),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}", color = Color.White.copy(alpha = 0.6f))
                                        Text(text = "Tasks: ${activity.completed}/${activity.total} | Score: $score/10", fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "${(activity.percentage * 100).toInt()}% Done", style = MaterialTheme.typography.labelSmall, color = if (activity.percentage >= 0.8f) MintGreen else SolarYellow)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (period == StatsPeriod.MONTHLY) {
                var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
                val monthlySummary = remember(heatmapData, displayData) {
                    val activeDays = displayData.count { (heatmapData[it.toString()]?.percentage ?: 0f) > 0f }
                    val avgCompletion = if (displayData.isNotEmpty()) {
                        displayData.map { heatmapData[it.toString()]?.percentage ?: 0f }.average() * 100
                    } else 0.0
                    
                    var maxStreak = 0
                    var currentStreak = 0
                    displayData.forEach { date ->
                        if ((heatmapData[date.toString()]?.percentage ?: 0f) >= 0.8f) {
                            currentStreak++; maxStreak = maxOf(maxStreak, currentStreak)
                        } else currentStreak = 0
                    }
                    val topDay = displayData.maxByOrNull { heatmapData[it.toString()]?.percentage ?: 0f }
                    val topDayInfo = if (topDay != null) "${topDay.dayOfMonth}" else "None"
                    
                    Quadruple(activeDays, avgCompletion.toInt(), maxStreak, topDayInfo)
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryItem(label = "Active", value = "${monthlySummary.first}d", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Avg.", value = "${monthlySummary.second}%", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Streak", value = "${monthlySummary.third}🔥", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Top Day", value = monthlySummary.fourth, modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(text = day, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
                        }
                    }

                    val calendarRows = remember(displayData) {
                        val rows = mutableListOf<List<LocalDate>>()
                        var i = 0
                        while (i < displayData.size) {
                            val week = mutableListOf<LocalDate>()
                            repeat(7) { if (i < displayData.size) { week.add(displayData[i]); i++ } }
                            rows.add(week)
                        }
                        rows
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        calendarRows.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                week.forEach { date ->
                                    val activity = heatmapData[date.toString()] ?: DayActivity()
                                    val completion = activity.percentage
                                    val isSelected = selectedDate == date
                                    val color = when {
                                        completion >= 1f -> MintGreen
                                        completion >= 0.8f -> MintGreen.copy(alpha = 0.8f)
                                        completion >= 0.5f -> MintGreen.copy(alpha = 0.5f)
                                        completion >= 0.2f -> MintGreen.copy(alpha = 0.2f)
                                        else -> Color.White.copy(alpha = 0.1f)
                                    }
                                    val scale by animateFloatAsState(if (isSelected) 1.25f else 1f)
                                    Box(
                                        modifier = Modifier.size(32.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(RoundedCornerShape(8.dp)).background(color)
                                            .border(width = if (isSelected) 2.dp else 0.dp, color = ElectricBlue, shape = RoundedCornerShape(8.dp))
                                            .clickable { selectedDate = if (selectedDate == date) null else date }
                                    )
                                }
                                if (week.size < 7) repeat(7 - week.size) { Spacer(modifier = Modifier.size(32.dp)) }
                            }
                        }
                    }
                    
                    androidx.compose.animation.AnimatedVisibility(visible = selectedDate != null, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            selectedDate?.let { date ->
                                val activity = heatmapData[date.toString()] ?: DayActivity()
                                val score = (activity.percentage * 10).toInt()
                                Surface(
                                    modifier = Modifier.padding(top = 16.dp).clip(RoundedCornerShape(12.dp)),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}", color = Color.White.copy(alpha = 0.6f))
                                        Text(text = "Tasks: ${activity.completed}/${activity.total} | Score: $score/10", fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "${(activity.percentage * 100).toInt()}% Done", style = MaterialTheme.typography.labelSmall, color = if (activity.percentage >= 0.8f) MintGreen else SolarYellow)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // True Continuous Yearly Heatmap (GitHub Style)
                var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
                val yearlySummary = remember(heatmapData, displayData) {
                    val activeDays = displayData.count { (heatmapData[it.toString()]?.percentage ?: 0f) > 0f }
                    val avgCompletion = if (displayData.isNotEmpty()) {
                        displayData.map { heatmapData[it.toString()]?.percentage ?: 0f }.average() * 100
                    } else 0.0
                    var maxStreak = 0
                    var currentStreak = 0
                    displayData.forEach { date ->
                        if ((heatmapData[date.toString()]?.percentage ?: 0f) >= 0.8f) {
                            currentStreak++; maxStreak = maxOf(maxStreak, currentStreak)
                        } else currentStreak = 0
                    }
                    val monthStats = displayData.groupBy { it.monthNumber }.mapValues { it.value.map { heatmapData[it.toString()]?.percentage ?: 0f }.average() }
                    val topMonthNum = monthStats.maxByOrNull { it.value }?.key ?: 1
                    val topMonthName = LocalDate(2024, topMonthNum, 1).month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    
                    Quadruple(activeDays, avgCompletion.toInt(), maxStreak, topMonthName)
                }

                val yearlyMonths = remember(displayData) {
                    displayData.groupBy { it.month }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryItem(label = "Active", value = "${yearlySummary.first}d", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Avg.", value = "${yearlySummary.second}%", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Streak", value = "${yearlySummary.third}🔥", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Top", value = yearlySummary.fourth, modifier = Modifier.weight(1f))
                    }

                    // Month-by-month grid (3 columns)
                    val monthChunks = remember(yearlyMonths) { yearlyMonths.entries.chunked(3) }
                    
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        monthChunks.forEach { chunk ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                chunk.forEach { (month, days) ->
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        // Mini 7-row heatmap for this month
                                        val monthWeeks = remember(days) {
                                            val first = days.first()
                                            val last = days.last()
                                            var current = first.minus(first.dayOfWeek.ordinal, DateTimeUnit.DAY)
                                            val columns = mutableListOf<List<LocalDate?>>()
                                            while (current <= last) {
                                                val week = mutableListOf<LocalDate?>()
                                                repeat(7) {
                                                    if (current >= first && current <= last) week.add(current) else week.add(null)
                                                    current = current.plus(1, DateTimeUnit.DAY)
                                                }
                                                columns.add(week)
                                            }
                                            columns
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            monthWeeks.forEach { week ->
                                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    week.forEach { date ->
                                                        if (date != null) {
                                                            val activity = heatmapData[date.toString()] ?: DayActivity()
                                                            val isSelected = selectedDate == date
                                                            val color = when {
                                                                activity.percentage >= 1f -> MintGreen
                                                                activity.percentage >= 0.8f -> MintGreen.copy(alpha = 0.8f)
                                                                activity.percentage >= 0.5f -> MintGreen.copy(alpha = 0.5f)
                                                                activity.percentage >= 0.2f -> MintGreen.copy(alpha = 0.2f)
                                                                else -> Color.White.copy(alpha = 0.08f)
                                                            }
                                                            val scale by animateFloatAsState(if (isSelected) 1.4f else 1f)
                                                            Box(modifier = Modifier.size(10.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(RoundedCornerShape(2.dp)).background(color)
                                                                .border(width = if (isSelected) 1.dp else 0.dp, color = ElectricBlue, shape = RoundedCornerShape(2.dp))
                                                                .clickable { selectedDate = if (selectedDate == date) null else date })
                                                        } else Spacer(modifier = Modifier.size(10.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (chunk.size < 3) repeat(3 - chunk.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                    
                    androidx.compose.animation.AnimatedVisibility(visible = selectedDate != null, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            selectedDate?.let { date ->
                                val activity = heatmapData[date.toString()] ?: DayActivity()
                                val score = (activity.percentage * 10).toInt()
                                Surface(
                                    modifier = Modifier.padding(top = 16.dp).clip(RoundedCornerShape(10.dp)),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}", color = Color.White.copy(alpha = 0.6f))
                                        Text(text = "Tasks: ${activity.completed}/${activity.total} | Score: $score/10", fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "${(activity.percentage * 100).toInt()}% Done", style = MaterialTheme.typography.labelSmall, color = if (activity.percentage >= 0.8f) MintGreen else SolarYellow)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text("Less", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.width(4.dp))
            listOf(0.08f, 0.2f, 0.5f, 0.8f, 1f).forEach { alpha ->
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MintGreen.copy(alpha = alpha)))
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text("More", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun remember(factory: () -> StatsViewModel): StatsViewModel {
    return androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return factory() as T
        }
    })
}
