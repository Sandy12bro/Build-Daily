package com.example.builddaily.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.viewmodel.InsightsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = koinViewModel()
) {
    val days by viewModel.days.collectAsState()
    val today = LocalDate.now()
    
    // Prepare data for last 7 days
    val weeklyData = (6 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val day = days.find { it.date == dateString }
        (day?.completionRate ?: 0f) * 100f
    }.toTypedArray()
    
    val weeklyChartModel = entryModelOf(*weeklyData)

    // Prepare data for last 30 days
    val monthlyData = (29 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val day = days.find { it.date == dateString }
        (day?.completionRate ?: 0f) * 100f
    }.toTypedArray()

    val monthlyChartModel = entryModelOf(*monthlyData)

    var selectedTabIndex by remember { mutableStateOf(1) } // Default to Week
    val tabs = listOf("Day", "Week", "Month", "Year")

    // Yearly Data Calculation
    val yearlyData = (11 downTo 0).map { monthOffset ->
        val targetMonth = today.minusMonths(monthOffset.toLong())
        val daysInMonth = days.filter { 
            val date = LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
            date.year == targetMonth.year && date.month == targetMonth.month
        }
        if (daysInMonth.isEmpty()) 0f else (daysInMonth.sumOf { it.completionRate.toDouble() } / daysInMonth.size).toFloat() * 100f
    }.toTypedArray()
    val yearlyChartModel = entryModelOf(*yearlyData)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> { // Day
                        val todayData = days.find { it.date == today.format(DateTimeFormatter.ISO_LOCAL_DATE) }
                        val rate = todayData?.completionRate ?: 0f
                        Text("Today's Completion", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(
                                progress = rate,
                                modifier = Modifier.size(150.dp),
                                strokeWidth = 12.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text("${(rate * 100).toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    1 -> { // Week
                        Text("Weekly Completion (%)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        Chart(
                            chart = columnChart(),
                            model = weeklyChartModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(200.dp)
                        )
                    }
                    2 -> { // Month
                        Text("30-Day Trend", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        Chart(
                            chart = lineChart(),
                            model = monthlyChartModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(200.dp)
                        )
                    }
                    3 -> { // Year
                        Text("12-Month Average (%)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        Chart(
                            chart = columnChart(),
                            model = yearlyChartModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
