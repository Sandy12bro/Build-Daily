package com.example.builddaily.ui.history

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
import androidx.compose.ui.graphics.Color as GraphicsColor
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GraphicsColor.Transparent),
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
                        HistoryTimelineItem(
                            index = index,
                            task = task,
                            totalTasks = tasks.size,
                            onToggleComplete = { 
                                com.example.builddaily.util.ActionMessageManager.postMessage(
                                    "History records cannot be modified 🔒", 
                                    com.example.builddaily.util.ActionType.INCOMPLETE
                                )
                            },
                            onDelete = { 
                                com.example.builddaily.util.ActionMessageManager.postMessage(
                                    "History cannot be deleted 🔒", 
                                    com.example.builddaily.util.ActionType.INCOMPLETE
                                )
                            },
                            onEdit = { 
                                com.example.builddaily.util.ActionMessageManager.postMessage(
                                    "Completed history cannot be edited ✨", 
                                    com.example.builddaily.util.ActionType.INCOMPLETE
                                )
                            },
                            onRepeat = { viewModel.repeatTask(task) }
                        )
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

@Composable
private fun HistoryTimelineItem(
    index: Int,
    task: com.example.builddaily.data.model.Task,
    totalTasks: Int,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onRepeat: () -> Unit
) {
    val taskColor = remember(task.title, task.colorHex) {
        if (!task.colorHex.isNullOrBlank()) {
            try { GraphicsColor(task.colorHex.toColorInt()) } catch (_: Exception) { com.example.builddaily.ui.theme.CyberPurple }
        } else {
            val colorIndex = kotlin.math.abs(task.title.hashCode()) % com.example.builddaily.ui.theme.TaskCategoryColors.size
            com.example.builddaily.ui.theme.TaskCategoryColors[colorIndex]
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
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
                                if (index == 0) GraphicsColor.Transparent else GraphicsColor.White.copy(alpha = 0.05f),
                                GraphicsColor.White.copy(alpha = 0.15f)
                            )
                        )
                    )
            )
            
            // The schedule dot with glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val dotColor = if (task.isCompleted) {
                    GraphicsColor.White.copy(alpha = 0.2f)
                } else {
                    taskColor
                }
                if (!task.isCompleted) {
                    // Subtle outer glow
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(taskColor.copy(alpha = 0.2f), GraphicsColor.Transparent)
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
                            if (task.isCompleted) GraphicsColor.White.copy(alpha = 0.15f) 
                            else dotColor
                        )
                        .border(
                            width = 1.dp,
                            color = if (task.isCompleted) GraphicsColor.Transparent else GraphicsColor.White.copy(alpha = 0.4f),
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
                                GraphicsColor.White.copy(alpha = 0.15f),
                                if (index == totalTasks - 1) GraphicsColor.Transparent else GraphicsColor.White.copy(alpha = 0.05f)
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
                onToggleComplete = onToggleComplete,
                onDelete = onDelete,
                onEdit = onEdit,
                onRepeat = onRepeat
            )
        }
    }
}
