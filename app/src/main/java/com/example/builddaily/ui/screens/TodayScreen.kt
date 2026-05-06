package com.example.builddaily.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import kotlin.math.roundToInt
import com.example.builddaily.data.models.EnergyType
import com.example.builddaily.data.models.Task
import com.example.builddaily.data.models.TaskStatus
import com.example.builddaily.ui.theme.Gray50
import com.example.builddaily.ui.theme.White
import com.example.builddaily.viewmodel.TodayViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = koinViewModel(),
    onNavigateToPlan: () -> Unit
) {
    val todayDay by viewModel.todayDay.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val displayDate = viewModel.getDisplayDate()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with date and progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    todayDay?.let { day ->
                        Text(
                            text = "${(day.completionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                todayDay?.let { day ->
                    LinearProgressIndicator(
                        progress = day.completionRate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Tasks list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .animateContentSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleStatus = { viewModel.toggleTaskStatus(task) },
                        onDelete = { /* TODO: Implement delete */ }
                    )
                }
            }
            
            // Plan Day button
            Button(
                onClick = onNavigateToPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = White
                )
            ) {
                Text(
                    text = "Plan Day",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        offsetX += delta
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(x = offsetX.roundToInt(), y = 0) }
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStarted = { /* Optional: Handle drag start */ },
                onDragStopped = {
                    if (offsetX < -100) {
                        // Swipe left - Mark as missed
                        onToggleStatus()
                        offsetX = 0f
                    } else if (offsetX > 100) {
                        // Swipe right - Mark as done
                        onToggleStatus()
                        offsetX = 0f
                    } else {
                        offsetX = 0f
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.DONE -> Gray50
                TaskStatus.MISSED -> Gray50
                else -> White
            },
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Energy type indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (task.energyType) {
                            EnergyType.DEEP -> MaterialTheme.colorScheme.primary
                            EnergyType.LIGHT -> MaterialTheme.colorScheme.secondary
                        },
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (task.energyType) {
                        EnergyType.DEEP -> "D"
                        EnergyType.LIGHT -> "L"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Task title
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (task.status == TaskStatus.DONE) FontWeight.Normal else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.status == TaskStatus.DONE) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    maxLines = 2
                )
                
                task.time?.let { time ->
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Status indicator
            when (task.status) {
                TaskStatus.PENDING -> {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                TaskStatus.DONE -> {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                TaskStatus.MISSED -> {
                    Text(
                        text = "Missed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}
