package com.example.builddaily.ui.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.*
import com.example.builddaily.data.repository.TodoListRepository
import com.example.builddaily.ui.components.AppTitleWithLogo
import com.example.builddaily.ui.theme.*
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onBack: () -> Unit, 
    onNavigateToAddTodo: () -> Unit,
    statsRepository: com.example.builddaily.data.repository.UserStatsRepository
) {
    val context = LocalContext.current
    val repository = remember { TodoListRepository(context) }
    val viewModel = remember { TodoListViewModel(repository, statsRepository) }
    
    val activeTodos by viewModel.activeTodos.collectAsState(initial = emptyList())
    val archivedTodos by viewModel.archivedTodos.collectAsState(initial = emptyList())
    val allTodos by viewModel.allTodos.collectAsState(initial = emptyList())
    val sortOption by viewModel.sortOption.collectAsState()
    
    var showArchive by remember { mutableStateOf(false) }
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        NebulaBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { AppTitleWithLogo(if (showArchive) "Mission Archive" else "Active Missions", showLogo = false) },
                    navigationIcon = {
                        IconButton(onClick = if (showArchive) { { showArchive = false } } else onBack) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showArchive = !showArchive }) {
                            Icon(
                                if (showArchive) Icons.Default.List else Icons.Default.History, 
                                contentDescription = "Archive", 
                                tint = if (showArchive) ElectricBlue else Color.White
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!showArchive) {
                    FloatingActionButton(
                        onClick = onNavigateToAddTodo,
                        containerColor = ElectricBlue,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (!showArchive) {
                    TodoSortHeader(
                        currentSort = sortOption,
                        onSortSelected = { viewModel.setSortOption(it) }
                    )
                }

                Crossfade(targetState = showArchive, animationSpec = tween(500)) { isArchive ->
                    if (isArchive) {
                        ArchiveView(
                            todos = archivedTodos,
                            onRestore = { viewModel.restoreTodo(it) },
                            onDeletePermanently = { viewModel.deleteTodo(it.id) }
                        )
                    } else {
                        ActiveTasksView(
                            todos = activeTodos,
                            allTodos = allTodos,
                            viewModel = viewModel,
                            expandedSections = expandedSections
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodoSortHeader(currentSort: TodoSortOption, onSortSelected: (TodoSortOption) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TodoSortOption.entries.forEach { option ->
            val isSelected = currentSort == option
            Surface(
                onClick = { onSortSelected(option) },
                color = if (isSelected) ElectricBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(1.dp, ElectricBlue) else null
            ) {
                Text(
                    text = option.name.replace("_", " ").lowercase().capitalize(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.5f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ActiveTasksView(
    todos: List<TodoItem>,
    allTodos: List<TodoItem>,
    viewModel: TodoListViewModel,
    expandedSections: MutableMap<String, Boolean>
) {
    val groupedTodos = remember(todos) {
        todos.groupBy { getTodoGroup(it.deadline) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        item {
            PremiumMissionHUD(allTodos)
        }

        if (todos.isEmpty()) {
            item { EmptyMissionsState() }
        } else {
            TodoGroup.entries.forEach { group ->
                val items = groupedTodos[group] ?: emptyList()
                if (items.isNotEmpty()) {
                    item {
                        val isExpanded = expandedSections[group.name] ?: true
                        TodoSectionHeader(
                            group = group,
                            count = items.size,
                            isExpanded = isExpanded,
                            onToggle = { expandedSections[group.name] = !isExpanded }
                        )
                    }

                    if (expandedSections[group.name] ?: true) {
                        items(items, key = { it.id }) { todo ->
                            BeautifulTodoCard(
                                todo = todo,
                                onToggle = { 
                                    viewModel.toggleTodo(todo)
                                    ActionMessageManager.postMessage("Mission Accomplished! 🎖️", ActionType.COMPLETED)
                                },
                                onDelete = { 
                                    viewModel.deleteTodo(todo.id)
                                    ActionMessageManager.postMessage("Data purged.", ActionType.DELETED)
                                },
                                onAddSubTask = { title -> viewModel.addSubTask(todo, title) },
                                onToggleSubTask = { subId -> viewModel.toggleSubTask(todo, subId) },
                                onDeleteSubTask = { subId -> viewModel.deleteSubTask(todo, subId) },
                                onUpdateTodo = { title, cat, prio, dl, notes, tags, rem ->
                                    viewModel.updateTodo(todo.id, title, cat, prio, dl, notes, tags, rem)
                                },
                                onUpdateSubTaskTitle = { subId, title ->
                                    viewModel.updateSubTaskTitle(todo.id, subId, title)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoSectionHeader(group: TodoGroup, count: Int, isExpanded: Boolean, onToggle: () -> Unit) {
    val color = when(group) {
        TodoGroup.OVERDUE -> FlareRed
        TodoGroup.TODAY -> SolarYellow
        TodoGroup.TOMORROW -> ElectricBlue
        TodoGroup.THIS_WEEK -> MintGreen
        TodoGroup.LATER -> MutedSlate
    }

    val icon = when(group) {
        TodoGroup.OVERDUE -> Icons.Default.Warning
        TodoGroup.TODAY -> Icons.Default.LocalFireDepartment
        TodoGroup.TOMORROW -> Icons.Default.Schedule
        TodoGroup.THIS_WEEK -> Icons.Default.Event
        TodoGroup.LATER -> Icons.Default.CalendarToday
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = group.name.replace("_", " "),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(count.toString(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ArchiveView(
    todos: List<TodoItem>,
    onRestore: (TodoItem) -> Unit,
    onDeletePermanently: (TodoItem) -> Unit
) {
    val groupedArchive = remember(todos) {
        todos.groupBy { getArchiveCategory(it.completionTime) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        if (todos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(80.dp).alpha(0.1f), tint = Color.White)
                    Text("ARCHIVE EMPTY", color = Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                }
            }
        } else {
            listOf("Today Completed", "This Week", "This Month", "Older").forEach { cat ->
                val items = groupedArchive[cat] ?: emptyList()
                if (items.isNotEmpty()) {
                    item {
                        Text(
                            cat.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(items, key = { it.id }) { todo ->
                        ArchiveCard(todo, onRestore, onDeletePermanently)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveCard(todo: TodoItem, onRestore: (TodoItem) -> Unit, onDeletePermanently: (TodoItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(todo.title, color = Color.White.copy(alpha = 0.5f), textDecoration = TextDecoration.LineThrough)
                todo.completionTime?.let {
                    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                    Text("Completed: ${sdf.format(Date(it))}", style = MaterialTheme.typography.labelSmall, color = MintGreen.copy(alpha = 0.4f))
                }
            }
            IconButton(onClick = { onRestore(todo) }) {
                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = ElectricBlue)
            }
            IconButton(onClick = { onDeletePermanently(todo) }) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Permanently", tint = FlareRed.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun BeautifulTodoCard(
    todo: TodoItem, 
    onToggle: () -> Unit, 
    onDelete: () -> Unit,
    onAddSubTask: (String) -> Unit,
    onToggleSubTask: (String) -> Unit,
    onDeleteSubTask: (String) -> Unit,
    onUpdateTodo: (String, String, TodoPriority, Long?, String, List<String>, Boolean) -> Unit,
    onUpdateSubTaskTitle: (String, String) -> Unit
) {
    val context = LocalContext.current
    val categoryColor = getCategoryColor(todo.category)
    val isOverdue = remember(todo.deadline) { 
        todo.deadline?.let { it < System.currentTimeMillis() } ?: false 
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val currentShake = if (isOverdue && !todo.isCompleted) shakeOffset else 0f

    val priorityColor = when(todo.priority) {
        TodoPriority.CRITICAL -> FlareRed
        TodoPriority.HIGH -> SolarYellow
        TodoPriority.MEDIUM -> ElectricBlue
        TodoPriority.LOW -> MutedSlate
    }

    var isExpanded by remember { mutableStateOf(false) }
    var newSubTaskTitle by remember { mutableStateOf("") }
    var showEditTodoDialog by remember { mutableStateOf(false) }
    var subTaskToEdit by remember { mutableStateOf<com.example.builddaily.data.model.SubTask?>(null) }

    val dateText = remember(todo.createdAt) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(todo.createdAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = currentShake.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .then(
                if (todo.priority == TodoPriority.CRITICAL && !todo.isCompleted) {
                    Modifier.border(
                        1.dp,
                        priorityColor.copy(alpha = glowAlpha),
                        RoundedCornerShape(24.dp)
                    )
                } else if (isOverdue && !todo.isCompleted) {
                    Modifier.border(1.dp, FlareRed.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                } else {
                    Modifier.border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(categoryColor.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        RoundedCornerShape(24.dp)
                    )
                }
            )
            .clickable { isExpanded = !isExpanded }
            .then(
                if (todo.isCompleted) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Modifier.border(2.dp, priorityColor.copy(alpha = pulseAlpha), RoundedCornerShape(24.dp))
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = todo.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue && !todo.isCompleted) FlareRed else categoryColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        if (todo.hasReminder) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.NotificationsActive, null, tint = SolarYellow, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                        lineHeight = 28.sp
                    )
                    
                    if (isOverdue && !todo.isCompleted) {
                        Text(
                            "⚠ MISSION OVERDUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = FlareRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Task Progress Bar (New Requirement)
                    if (todo.subtasks.isNotEmpty()) {
                        val subCompleted = todo.subtasks.count { it.isCompleted }
                        val subTotal = todo.subtasks.size
                        val subProgress = subCompleted.toFloat() / subTotal
                        
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$subCompleted / $subTotal COMPLETED • ${(subProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { subProgress },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = priorityColor,
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                        }
                    }

                    // Tags
                    if (todo.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            todo.tags.take(3).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("#$tag", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                
                // Priority Indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (todo.isCompleted) Color.White.copy(alpha = 0.05f) else priorityColor.copy(alpha = 0.1f))
                        .clickable { onToggle() }
                        .border(1.dp, if (todo.isCompleted) Color.White.copy(alpha = 0.1f) else priorityColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (todo.isCompleted) Icons.Default.Check else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (todo.isCompleted) Color.White.copy(alpha = 0.3f) else priorityColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Subtasks Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (todo.notes.isNotBlank()) {
                        Text(
                            todo.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    todo.subtasks.forEach { subTask ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subTask.isCompleted,
                                onCheckedChange = { onToggleSubTask(subTask.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = categoryColor,
                                    uncheckedColor = Color.White.copy(alpha = 0.3f),
                                    checkmarkColor = Color.Black
                                )
                            )
                            Text(
                                text = subTask.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (subTask.isCompleted) Color.White.copy(alpha = 0.3f) else Color.White,
                                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteSubTask(subTask.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Add Subtask Input
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newSubTaskTitle,
                            onValueChange = { newSubTaskTitle = it },
                            placeholder = { Text("Add subtask...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = categoryColor,
                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            if (newSubTaskTitle.isNotBlank()) {
                                onAddSubTask(newSubTaskTitle)
                                newSubTaskTitle = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, null, tint = categoryColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deadline
                    todo.deadline?.let { dl ->
                        val dlText = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(dl))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isOverdue) Icons.Default.Warning else Icons.Default.Event,
                                null,
                                tint = if (isOverdue) FlareRed else SolarYellow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dlText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) FlareRed else SolarYellow,
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showEditTodoDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, null, tint = FlareRed.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

    // Modal Edit Task Dialog
    if (showEditTodoDialog) {
        var editedTitle by remember { mutableStateOf(todo.title) }
        var editedCategory by remember { mutableStateOf(todo.category) }
        var editedPriority by remember { mutableStateOf(todo.priority) }
        var editedDeadline by remember { mutableStateOf(todo.deadline) }
        var editedNotes by remember { mutableStateOf(todo.notes) }
        var editedTagsInput by remember { mutableStateOf(todo.tags.joinToString(", ")) }
        var editedReminder by remember { mutableStateOf(todo.hasReminder) }

        val deadlineText = remember(editedDeadline) {
            editedDeadline?.let {
                val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                sdf.format(Date(it))
            } ?: "No deadline set"
        }

        fun showEditDateTimePicker() {
            val currentDateTime = Calendar.getInstance()
            val startYear = currentDateTime.get(Calendar.YEAR)
            val startMonth = currentDateTime.get(Calendar.MONTH)
            val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
            val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
            val startMinute = currentDateTime.get(Calendar.MINUTE)

            DatePickerDialog(context, { _, year, month, day ->
                TimePickerDialog(context, { _, hour, minute ->
                    val pickedDateTime = Calendar.getInstance()
                    pickedDateTime.set(year, month, day, hour, minute)
                    editedDeadline = pickedDateTime.timeInMillis
                }, startHour, startMinute, false).show()
            }, startYear, startMonth, startDay).show()
        }

        AlertDialog(
            onDismissRequest = { showEditTodoDialog = false },
            containerColor = DeepVoid,
            title = { Text("Mission Configuration", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Title", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = ElectricBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        label = { Text("Notes", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = ElectricBlue),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    // Deadline Picker
                    Surface(
                        onClick = { showEditDateTimePicker() },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = categoryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(deadlineText, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            if (editedDeadline != null) {
                                TextButton(onClick = { editedDeadline = null }) {
                                    Text("Clear", color = FlareRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    
                    // Priority Switcher
                    Column {
                        Text("Priority", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 8.dp)) {
                            TodoPriority.entries.forEach { p ->
                                val sel = editedPriority == p
                                val pCol = when(p) {
                                    TodoPriority.CRITICAL -> FlareRed
                                    TodoPriority.HIGH -> SolarYellow
                                    TodoPriority.MEDIUM -> ElectricBlue
                                    TodoPriority.LOW -> MutedSlate
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) pCol.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, if (sel) pCol else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { editedPriority = p }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(p.name, color = if (sel) pCol else Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editedTagsInput,
                        onValueChange = { editedTagsInput = it },
                        label = { Text("Tags (comma separated)", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = ElectricBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = editedReminder,
                            onCheckedChange = { editedReminder = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectricBlue)
                        )
                        Text("Enable Notifications", color = Color.White, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tags = editedTagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onUpdateTodo(editedTitle, editedCategory, editedPriority, editedDeadline, editedNotes, tags, editedReminder)
                        showEditTodoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Confirm Sync", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTodoDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.4f))
                }
            }
        )
    }
}

    // Modal Edit Subtask Dialog
    subTaskToEdit?.let { st ->
        var editedSubTitle by remember { mutableStateOf(st.title) }
        AlertDialog(
            onDismissRequest = { subTaskToEdit = null },
            containerColor = DeepVoid,
            titleContentColor = Color.White,
            title = { Text("Edit Subtask", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedSubTitle,
                    onValueChange = { editedSubTitle = it },
                    label = { Text("Subtask Title", color = Color.White.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = categoryColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedSubTitle.isNotBlank()) {
                            onUpdateSubTaskTitle(st.id, editedSubTitle)
                            subTaskToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
                ) {
                    Text("Save", color = SpaceBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subTaskToEdit = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun PremiumMissionHUD(todos: List<TodoItem>) {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Weighted progress calculation
    val todayTasks = todos.filter { it.isCompleted && (it.completionTime ?: 0L) >= todayStart || !it.isCompleted }
    
    val totalWeight = todayTasks.size.toFloat()
    val completedWeight = todayTasks.sumOf { todo ->
        if (todo.subtasks.isEmpty()) {
            if (todo.isCompleted) 1.0 else 0.0
        } else {
            todo.subtasks.count { it.isCompleted }.toDouble() / todo.subtasks.size
        }
    }.toFloat()

    val progress by animateFloatAsState(
        targetValue = if (totalWeight > 0) completedWeight / totalWeight else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing)
    )

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360
    val titleSize = if (isSmallScreen) 20.sp else 28.sp
    val percentSize = if (isSmallScreen) 34.sp else 48.sp

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
            .height(if (isSmallScreen) 160.dp else 180.dp)
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
                modifier = Modifier.fillMaxWidth().padding(bottom = if (isSmallScreen) 8.dp else 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "MISSION CONTROL",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = if (isSmallScreen) 1.sp else 2.sp,
                        fontSize = if (isSmallScreen) 9.sp else 11.sp
                    )
                    Text(
                        "Daily Objectives",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = titleSize,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = percentSize,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (progress >= 1f) MintGreen else ElectricBlue))
                        Spacer(modifier = Modifier.width(8.dp))
                        val completedDisplay = if (completedWeight % 1f == 0f) completedWeight.toInt().toString() else String.format("%.1f", completedWeight)
                        Text(
                            "$completedDisplay / ${totalWeight.toInt()} MISSIONS SECURED",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        if (progress >= 1f) "ALL MISSIONS CLEAR" else "ACTIVE OPERATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (progress >= 1f) MintGreen else ElectricBlue,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 8.dp else 10.dp).clip(CircleShape),
                    color = ElectricBlue,
                    trackColor = Color.White.copy(alpha = 0.05f)
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


