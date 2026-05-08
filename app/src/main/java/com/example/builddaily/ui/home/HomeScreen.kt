package com.example.builddaily.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.graphics.toColorInt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import com.example.builddaily.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as GraphicsColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.ui.components.TaskCard
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.util.formatDisplay
import com.example.builddaily.util.today

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: TaskRepository,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit
) {
    val viewModel = remember { HomeViewModel(repository) }
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showConfetti by remember { mutableStateOf(false) }
    var lastCompletedCount by remember { mutableIntStateOf(-1) }
    
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    // Detect when all tasks are completed
    LaunchedEffect(tasks) {
        val completedCount = tasks.count { it.isCompleted }
        val totalCount = tasks.size
        
        // Trigger confetti only if we just reached 100% and we had tasks
        if (totalCount > 0 && completedCount == totalCount && lastCompletedCount != -1 && lastCompletedCount < totalCount) {
            showConfetti = true
        }
        lastCompletedCount = completedCount
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Galactic Background
        com.example.builddaily.ui.components.GalacticBackground()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            GraphicsColor.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(1000f, 0f),
                        radius = 800f
                    )
                )
        )

        Scaffold(
            containerColor = GraphicsColor.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = GraphicsColor.Transparent),
                title = {
                    Column {
                        com.example.builddaily.ui.components.AppTitleWithLogo("Build Daily")
                        Text(
                            today().formatDisplay(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 44.dp)
                        )
                    }
                }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTask,
                    containerColor = ElectricBlue,
                    contentColor = GraphicsColor.Black,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add task", modifier = Modifier.size(28.dp))
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val completedCount = tasks.count { it.isCompleted }
                val totalCount = tasks.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Day Progress",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = GraphicsColor.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val emoji = when {
                                progress >= 1.0f -> "🤩"
                                progress >= 0.8f -> "😄"
                                progress >= 0.6f -> "😊"
                                progress >= 0.4f -> "🙂"
                                progress >= 0.2f -> "😐"
                                else -> "😢"
                            }
                            
                            AnimatedContent(
                                targetState = emoji,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.8f) togetherWith
                                            fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.8f)
                                },
                                label = "EmojiAnimation"
                            ) { targetEmoji ->
                                Text(
                                    text = targetEmoji,
                                    fontSize = 24.sp,
                                )
                            }
                        }
                        Text(
                            text = "$completedCount/$totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (progress >= 1.0f) com.example.builddaily.ui.theme.MintGreen else GraphicsColor.White.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .border(
                                width = 1.dp,
                                color = if (progress >= 1.0f) com.example.builddaily.ui.theme.MintGreen.copy(alpha = 0.3f) else GraphicsColor.Transparent,
                                shape = RoundedCornerShape(7.dp)
                            ),
                        color = if (progress >= 1.0f) com.example.builddaily.ui.theme.MintGreen else com.example.builddaily.ui.theme.CyberPurple,
                        trackColor = GraphicsColor.White.copy(alpha = 0.05f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.loadTasks() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        error != null && tasks.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        !isLoading && tasks.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No tasks for today", style = MaterialTheme.typography.titleMedium, color = GraphicsColor.White.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tap + to add a task",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                                    TaskTimelineItem(
                                        index = index,
                                        task = task,
                                        totalTasks = tasks.size,
                                        onToggleComplete = { _ -> 
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.toggleTaskCompletion(task) 
                                        },
                                        onDelete = { viewModel.deleteTask(task) },
                                        onEdit = { onEditTask(task.id) },
                                        onRepeat = { viewModel.repeatTask(task) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Celebration Overlay
        AnimatedVisibility(
            visible = showConfetti,
            enter = fadeIn(tween(800)) + scaleIn(tween(800, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(1000)) + scaleOut(tween(1000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GraphicsColor.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) { /* Block clicks to content behind */ },
                contentAlignment = Alignment.Center
            ) {
                com.example.builddaily.ui.components.ConfettiCelebration(onFinished = { showConfetti = false })
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(GraphicsColor.White.copy(alpha = 0.1f), GraphicsColor.Transparent)
                            ),
                            RoundedCornerShape(32.dp)
                        )
                        .border(1.dp, GraphicsColor.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .padding(32.dp)
                ) {
                    Text(
                        "🤩",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "WELL DONE!",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            brush = Brush.linearGradient(
                                colors = listOf(ElectricBlue, CyberPurple, MintGreen)
                            ),
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "All daily tasks completed",
                        style = MaterialTheme.typography.titleMedium,
                        color = GraphicsColor.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GraphicsColor.White.copy(alpha = 0.1f))
                            .clickable { showConfetti = false }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Continue", color = GraphicsColor.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskTimelineItem(
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
                            else taskColor
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
