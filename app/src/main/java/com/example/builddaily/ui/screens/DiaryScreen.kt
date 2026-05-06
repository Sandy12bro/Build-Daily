package com.example.builddaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.builddaily.viewmodel.DiaryViewModel
import org.koin.androidx.compose.koinViewModel

import com.example.builddaily.data.models.Mood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryViewModel = koinViewModel()
) {
    val diary by viewModel.diary.collectAsState()
    
    var text by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(Mood.NEUTRAL) }

    LaunchedEffect(diary) {
        diary?.let {
            text = it.text
            mood = it.mood
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reflection", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.saveDiary(text, mood)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                },
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("How are you feeling?", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoodSelector(
                    emoji = "😊",
                    label = "Good",
                    isSelected = mood == Mood.GOOD,
                    onClick = { mood = Mood.GOOD }
                )
                MoodSelector(
                    emoji = "😐",
                    label = "Neutral",
                    isSelected = mood == Mood.NEUTRAL,
                    onClick = { mood = Mood.NEUTRAL }
                )
                MoodSelector(
                    emoji = "😔",
                    label = "Bad",
                    isSelected = mood == Mood.BAD,
                    onClick = { mood = Mood.BAD }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Guided Prompts:", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• What actually mattered today?", fontSize = 14.sp, color = Color.Gray)
            Text("• What didn't go as planned?", fontSize = 14.sp, color = Color.Gray)
            Text("• One improvement for tomorrow?", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Write your thoughts here...", color = Color.LightGray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, lineHeight = 24.sp)
            )
        }
    }
}

@Composable
fun MoodSelector(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(emoji, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray)
    }
}
