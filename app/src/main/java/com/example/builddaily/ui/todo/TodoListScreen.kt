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
    val alpha by animateFloatAsState(if (todo.isCompleted) 0.7f else 1f)
    
    val dateText = remember(todo.createdAt) {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.ofEpochMilli(todo.createdAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            categoryColor.copy(alpha = 0.4f),
                            Color.Transparent,
                            categoryColor.copy(alpha = 0.1f)
                        )
                    )
                ),
                RoundedCornerShape(28.dp)
            )
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Checkbox
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (todo.isCompleted) categoryColor else Color.White.copy(alpha = 0.05f))
                    .border(2.dp, if (todo.isCompleted) Color.Transparent else categoryColor.copy(alpha = 0.5f), CircleShape)
                    .then(if (todo.isCompleted) Modifier.border(4.dp, Color.Black.copy(alpha = 0.1f), CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (todo.isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (todo.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Badge
                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(categoryColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = todo.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Date Label
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.03f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = FlareRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
fun NebulaBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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
fun PremiumMissionHUD(todos: List<TodoItem>) {
    val completed = todos.count { it.isCompleted }
    val total = todos.size
    val progress by animateFloatAsState(
        targetValue = if (total > 0) completed.toFloat() / total else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)),
                RoundedCornerShape(32.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            val yPos = size.height * (1f - progress)
            path.moveTo(0f, yPos)
            path.cubicTo(
                size.width * 0.25f, yPos - 20f,
                size.width * 0.75f, yPos + 20f,
                size.width, yPos
            )
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
            
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(MintGreen.copy(alpha = 0.2f), Color.Transparent)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STATUS HUD",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (total == 0) "Awaiting Tasks" else "Progress Level",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                    color = MintGreen,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
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



