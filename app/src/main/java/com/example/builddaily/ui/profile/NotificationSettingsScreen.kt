package com.example.builddaily.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.builddaily.ui.components.AppTitleWithLogo
import com.example.builddaily.util.NotificationHistoryManager
import com.example.builddaily.util.NotificationLog
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var history by remember { mutableStateOf(NotificationHistoryManager.getHistory(context)) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { AppTitleWithLogo("Notification Center", showLogo = false) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { 
                            NotificationHistoryManager.clearHistory(context)
                            history = emptyList()
                        }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
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
        ) {
            // Permission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasPermission) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                    else 
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (hasPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (hasPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (hasPermission) "Notifications Enabled" else "Notifications Disabled",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            if (hasPermission) "You will receive alerts at task start times." else "Enable alerts to never miss a task.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manage Permissions", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp).alpha(0.05f), tint = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("NO RECENT LOGS", color = Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(history) { log ->
                        NotificationLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationLogItem(log: NotificationLog) {
    val dateTime = Instant.fromEpochMilliseconds(log.timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    val dateStr = "${dateTime.dayOfMonth} ${dateTime.month.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(log.title, fontWeight = FontWeight.Bold, color = Color.White)
                if (log.description != null) {
                    Text(log.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(timeStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                Text(dateStr, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}
