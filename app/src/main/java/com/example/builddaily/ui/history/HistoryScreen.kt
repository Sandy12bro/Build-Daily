package com.example.builddaily.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.core.graphics.toColorInt
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.ui.components.TaskCard
import com.example.builddaily.util.formatDisplay
import com.example.builddaily.util.toEpochMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: TaskRepository,
    onEditTask: (String) -> Unit
) {
    val viewModel = remember { HistoryViewModel(repository) }
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        com.example.builddaily.ui.components.AppTitleWithLogo("History")
                        TextButton(
                            onClick = { showDatePicker = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.padding(start = 44.dp)
                        ) {
                            Text(
                                selectedDate.formatDisplay(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tasks.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No records found for this date", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Timeline visual elements
                            val taskColor = remember(task.title, task.colorHex) {
                                if (!task.colorHex.isNullOrBlank()) {
                                    try { Color(task.colorHex.toColorInt()) } catch (_: Exception) { MaterialTheme.colorScheme.primary }
                                } else {
                                    val colorIndex = kotlin.math.abs(task.title.hashCode()) % com.example.builddaily.ui.theme.TaskCategoryColors.size
                                    com.example.builddaily.ui.theme.TaskCategoryColors[colorIndex]
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(40.dp)
                            ) {
                                // Line above the dot
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .weight(1f)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    if (index == 0) Color.Transparent else Color.White.copy(alpha = 0.05f),
                                                    Color.White.copy(alpha = 0.15f)
                                                )
                                            )
                                        )
                                )
                                
                                // The schedule dot with glow
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    if (!task.isCompleted) {
                                        // Subtle outer glow
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(taskColor.copy(alpha = 0.2f), Color.Transparent)
                                                    ),
                                                    CircleShape
                                                )
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (task.isCompleted) Color.White.copy(alpha = 0.15f) 
                                                else taskColor
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (task.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                                
                                // Line below the dot
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .weight(1f)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.15f),
                                                    if (index == tasks.size - 1) Color.Transparent else Color.White.copy(alpha = 0.05f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            // Task card content
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 20.dp, start = 4.dp)
                            ) {
                                TaskCard(
                                    task = task,
                                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                                    onDelete = { viewModel.deleteTask(task) },
                                    onEdit = { onEditTask(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selected = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        viewModel.onDateSelected(selected)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
