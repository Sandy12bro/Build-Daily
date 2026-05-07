package com.example.builddaily.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.MintGreen
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

    LaunchedEffect(statsData) {
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

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val efficiency = if (statsData.overallTotal > 0) (statsData.overallCompleted * 100 / statsData.overallTotal) else 0

                    StatCard(label = "TOTAL", value = statsData.overallTotal.toString(), color = CyberPurple, modifier = Modifier.weight(1f))
                    StatCard(label = "DONE", value = statsData.overallCompleted.toString(), color = MintGreen, modifier = Modifier.weight(1f))
                    StatCard(label = "RATE", value = "$efficiency%", color = ElectricBlue, modifier = Modifier.weight(1f))
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
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom(),
                        ),
                        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
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
private fun remember(factory: () -> StatsViewModel): StatsViewModel {
    return androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return factory() as T
        }
    })
}
