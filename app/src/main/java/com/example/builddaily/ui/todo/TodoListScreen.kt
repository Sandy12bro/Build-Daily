package com.example.builddaily.ui.todo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.TodoItem
import com.example.builddaily.data.model.TodoPriority
import com.example.builddaily.data.repository.TodoListRepository
import com.example.builddaily.ui.components.AppTitleWithLogo
import com.example.builddaily.ui.theme.*
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(onBack: () -> Unit, onNavigateToAddTodo: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { TodoListRepository(context) }
    val viewModel = remember { TodoListViewModel(repository) }
    val todos by viewModel.todos.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        NebulaBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { AppTitleWithLogo("To-Do List", showLogo = false) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAddTodo,
                    containerColor = ElectricBlue,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
            ) {
                item {
                    PremiumMissionHUD(todos)
                }

                if (todos.isEmpty()) {
                    item {
                        EmptyMissionsState()
                    }
                } else {
                    items(todos, key = { it.id }) { todo ->
                        BeautifulTodoCard(
                            todo = todo,
                            onToggle = { 
                                viewModel.toggleTodo(todo)
                                val status = if (!todo.isCompleted) "completed! 🎉" else "moved to pending."
                                ActionMessageManager.postMessage("Task $status", if (!todo.isCompleted) ActionType.COMPLETED else ActionType.INCOMPLETE)
                            },
                            onDelete = { 
                                viewModel.deleteTodo(todo.id)
                                ActionMessageManager.postMessage("Task deleted forever.", ActionType.DELETED)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BeautifulTodoCard(todo: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    val categoryColor = getCategoryColor(todo.category)
    val scale by animateFloatAsState(if (todo.isCompleted) 0.98f else 1f)
    val alpha by animateFloatAsState(if (todo.isCompleted) 0.6f else 1f)
    
    val dateText = remember(todo.createdAt) {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.ofEpochMilli(todo.createdAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(categoryColor.copy(alpha = 0.4f), Color.Transparent)
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                    )
                }
                
                // Action Button (Check)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (todo.isCompleted) categoryColor else Color.White.copy(alpha = 0.05f))
                        .clickable { onToggle() }
                        .border(1.dp, if (todo.isCompleted) Color.Transparent else categoryColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (todo.isCompleted) Icons.Default.Check else Icons.Default.NorthEast,
                        contentDescription = null,
                        tint = if (todo.isCompleted) Color.Black else categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, null, tint = FlareRed.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumMissionHUD(todos: List<TodoItem>) {
    val completed = todos.count { it.isCompleted }
    val total = todos.size
    val progress by animateFloatAsState(
        targetValue = if (total > 0) completed.toFloat() / total else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
    ) {
        // Water/Wave Animation
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val fillHeight = height * (1f - progress)
            
            val path = Path()
            path.moveTo(0f, fillHeight)
            
            val waveAmplitude = 15f
            val waveLength = width / 1.5f
            
            for (x in 0..width.toInt()) {
                val y = fillHeight + (Math.sin((x / waveLength * 2 * Math.PI) + waveOffset.toDouble()).toFloat() * waveAmplitude)
                path.lineTo(x.toFloat(), y)
            }
            
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.4f), ElectricBlue.copy(alpha = 0.1f))
                )
            )
        }

        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "SYSTEM STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Workspace Fill",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$completed / $total Missions",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (progress >= 1f) "OPTIMIZED" else "STABILIZING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (progress >= 1f) MintGreen else ElectricBlue,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = ElectricBlue,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
            }
        }
    }
}

@Composable
fun NebulaBackground() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CyberPurple.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.3f),
                radius = 800f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElectricBlue.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.7f),
                radius = 1000f
            )
        )
    }
}

@Composable
fun EmptyMissionsState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = null,
            modifier = Modifier.size(100.dp).alpha(0.1f),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "NO ACTIVE TASKS",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.2f),
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Text(
            "Your workspace is clear.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.15f)
        )
    }
}


