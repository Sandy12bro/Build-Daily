package com.example.builddaily.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.builddaily.ui.components.AppTitleWithLogo

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent

import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.MenuBook

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToEvolution: () -> Unit,
    onNavigateToHydration: () -> Unit,
    onNavigateToBuyList: () -> Unit,
    onNavigateToBookLibrary: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { AppTitleWithLogo("More", showLogo = false) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoreOption(
                Icons.Default.Star, // Using Star for special evolution
                "Grow Your Life", 
                "Watch your personal plant evolve"
            ) {
                onNavigateToEvolution()
            }

            MoreOption(
                Icons.AutoMirrored.Filled.Assignment, 
                "TO-DO lists", 
                "Manage your long-term mission log"
            ) {
                onNavigateToTodoList()
            }

            MoreOption(
                Icons.Default.Timer, 
                "Pomodoro Timer", 
                "Focus with the galactic timer"
            ) {
                onNavigateToPomodoro()
            }

            MoreOption(
                Icons.Default.LocalDrink,
                "Water Intake & Hydration",
                "Track daily fluid & wellness vibes"
            ) {
                onNavigateToHydration()
            }

            MoreOption(
                Icons.Default.ShoppingCart,
                "Buy List & Budget",
                "Plan purchases & track savings"
            ) {
                onNavigateToBuyList()
            }

            MoreOption(
                Icons.Default.MenuBook,
                "Book Library",
                "Track reading & build knowledge"
            ) {
                onNavigateToBookLibrary()
            }

            MoreOption(
                Icons.Default.Notifications, 
                "Notifications", 
                "Task reminders and alerts"
            ) {
                onNavigateToNotifications()
            }
            
            MoreOption(
                Icons.Default.Share, 
                "Share App", 
                "Share the official download link"
            ) {
                val shareUrl = "https://github.com/Sandy12bro/Build-Daily/raw/main/BuildDaily.apk"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Build Daily - Elevate your productivity and watch your personal life tree grow! \uD83C\uDF0C\uD83C\uDF33\n\nDownload the latest version here: $shareUrl")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Link"))
            }
        }
    }
}

@Composable
fun MoreOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}
