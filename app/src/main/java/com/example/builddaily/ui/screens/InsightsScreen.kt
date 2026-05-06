package com.example.builddaily.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Weekly Completion (%)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = columnChart(),
                model = weeklyChartModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(200.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            Text("30-Day Trend", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = lineChart(),
                model = monthlyChartModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
