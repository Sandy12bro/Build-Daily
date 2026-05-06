package com.example.builddaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.builddaily.data.models.EnergyType
import com.example.builddaily.data.models.Task
import com.example.builddaily.viewmodel.PlanDayViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDayScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlanDayViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var energyType by remember { mutableStateOf(EnergyType.LIGHT) }
    var time by remember { mutableStateOf("") }

    val yesterdayTasks by viewModel.yesterdaysTasks.collectAsState()
    val selectedTasks = remember { mutableStateListOf<Task>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Day", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add New Task", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = energyType == EnergyType.LIGHT,
                        onClick = { energyType = EnergyType.LIGHT },
                        label = { Text("Light Energy") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    FilterChip(
                        selected = energyType == EnergyType.DEEP,
                        onClick = { energyType = EnergyType.DEEP },
                        label = { Text("Deep Energy") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.addTask(title, energyType, time)
                        title = ""
                        time = ""
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (yesterdayTasks.isNotEmpty()) {
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Repeat Yesterday", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Select tasks from yesterday to bring to today.", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(yesterdayTasks) { task ->
                    val isSelected = selectedTasks.contains(task)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable {
                                if (isSelected) selectedTasks.remove(task) else selectedTasks.add(task)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(task.title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                            if (!task.time.isNullOrBlank()) {
                                Text(task.time ?: "", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    if (selectedTasks.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.repeatTasks(selectedTasks)
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Repeat ${selectedTasks.size} Tasks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
