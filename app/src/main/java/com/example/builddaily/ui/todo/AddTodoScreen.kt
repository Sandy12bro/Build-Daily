package com.example.builddaily.ui.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.TodoPriority
import com.example.builddaily.data.repository.TodoListRepository
import com.example.builddaily.ui.theme.*
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreen(
    onBack: () -> Unit,
    statsRepository: com.example.builddaily.data.repository.UserStatsRepository
) {
    val context = LocalContext.current
    val repository = remember { TodoListRepository(context) }
    val viewModel = remember { TodoListViewModel(repository, statsRepository) }
    
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var priority by remember { mutableStateOf(TodoPriority.MEDIUM) }
    var deadline by remember { mutableStateOf<Long?>(null) }

    val categories = listOf("General", "Personal", "Work", "Health", "Study", "Finance")

    val deadlineText = remember(deadline) {
        deadline?.let {
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            sdf.format(Date(it))
        } ?: "No deadline set"
    }

    fun showDateTimePicker() {
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
                deadline = pickedDateTime.timeInMillis
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }

    Scaffold(
        containerColor = SpaceBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Create Task", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title Input
            Column {
                Text("What needs to be done?", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    placeholder = { Text("Task title...", color = Color.White.copy(alpha = 0.2f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedIndicatorColor = ElectricBlue,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Deadline Picker
            Column {
                Text("Deadline (Optional)", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showDateTimePicker() },
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = deadlineText,
                            color = if (deadline != null) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 15.sp,
                            fontWeight = if (deadline != null) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (deadline != null) {
                            TextButton(onClick = { deadline = null }) {
                                Text("Clear", color = FlareRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Category Selection
            Column {
                Text("Category", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        val catColor = getCategoryColor(cat)
                        Surface(
                            onClick = { category = cat },
                            color = if (isSelected) catColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.dp, catColor) else null
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = if (isSelected) catColor else Color.White.copy(alpha = 0.4f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Priority Selection
            Column {
                Text("Priority", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TodoPriority.values().forEach { p ->
                        val isSelected = priority == p
                        val pColor = when(p) {
                            TodoPriority.HIGH -> FlareRed
                            TodoPriority.MEDIUM -> ElectricBlue
                            TodoPriority.LOW -> MutedSlate
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) pColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                .border(1.dp, if (isSelected) pColor else Color.Transparent, RoundedCornerShape(16.dp))
                                .clickable { priority = p }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                p.name,
                                color = if (isSelected) pColor else Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    viewModel.addTodo(title, category, priority, deadline)
                    ActionMessageManager.postMessage("Task successfully launched! 🚀", ActionType.ADDED)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Task", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
