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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Settings

/**
 * Main "More" screen providing access to various utility modules and settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToEvolution: () -> Unit,
    onNavigateToHydration: () -> Unit,
    onNavigateToBuyList: () -> Unit,
    onNavigateToBookLibrary: () -> Unit,
) {
    val context = LocalContext.current

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { AppTitleWithLogo("More", showLogo = true) }
                )
            }
        ) { padding ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                val isWide = maxWidth > 600.dp
                val contentPadding = if (isWide) 32.dp else 24.dp
                val columnCount = if (maxWidth > 900.dp) 3 else if (isWide) 2 else 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding)
                ) {
                    SectionHeader("Utilities")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (columnCount > 1) {
                        // Adaptive Grid for Utilities
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            maxItemsInEachRow = columnCount
                        ) {
                            val utilityModifier = Modifier
                                .weight(1f)
                                .padding(bottom = 16.dp)
                                .widthIn(min = 200.dp)

                            MoreOption(Icons.Default.Timer, "Pomodoro Timer", "Focus sessions with break alerts", utilityModifier) { onNavigateToPomodoro() }
                            MoreOption(Icons.AutoMirrored.Filled.List, "Mission Logs", "Manage all your daily missions", utilityModifier) { onNavigateToTodoList() }
                            MoreOption(Icons.Default.AutoAwesome, "Life Evolution", "Watch your personal tree grow", utilityModifier) { onNavigateToEvolution() }
                            MoreOption(Icons.Default.WaterDrop, "Hydration Orbit", "Track daily water intake", utilityModifier) { onNavigateToHydration() }
                            MoreOption(Icons.Default.ShoppingCart, "Market List", "Plan your premium shopping", utilityModifier) { onNavigateToBuyList() }
                            MoreOption(Icons.Default.MenuBook, "Archive Library", "Track your knowledge growth", utilityModifier) { onNavigateToBookLibrary() }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MoreOption(Icons.Default.Timer, "Pomodoro Timer", "Focus sessions with break alerts") { onNavigateToPomodoro() }
                            MoreOption(Icons.AutoMirrored.Filled.List, "Mission Logs", "Manage all your daily missions") { onNavigateToTodoList() }
                            MoreOption(Icons.Default.AutoAwesome, "Life Evolution", "Watch your personal tree grow") { onNavigateToEvolution() }
                            MoreOption(Icons.Default.WaterDrop, "Hydration Orbit", "Track daily water intake") { onNavigateToHydration() }
                            MoreOption(Icons.Default.ShoppingCart, "Market List", "Plan your premium shopping") { onNavigateToBuyList() }
                            MoreOption(Icons.Default.MenuBook, "Archive Library", "Track your knowledge growth") { onNavigateToBookLibrary() }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader("Settings")
                    Spacer(modifier = Modifier.height(12.dp))

                    MoreOption(
                        Icons.Default.Settings,
                        "App Settings",
                        "Security, Notifications & Sharing",
                        modifier = if (isWide) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()
                    ) {
                        onNavigateToSettings()
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
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
