package com.example.builddaily.ui.profile

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.builddaily.ui.components.AppTitleWithLogo
import com.example.builddaily.ui.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateToSecurity: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val onNavigateToShare = {
        val shareUrl = "https://github.com/Sandy12bro/Build-Daily/raw/main/BuildDaily.apk"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Build Daily - Elevate your productivity and watch your personal life tree grow! 🌌🌳\n\nDownload the latest version here: $shareUrl")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Link"))
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { AppTitleWithLogo("Settings", showLogo = true) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ElectricBlue)
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isWide = maxWidth > 600.dp
            val contentPadding = if (isWide) 32.dp else 24.dp
            val columnCount = if (isWide) 2 else 1

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
            ) {
                Text(
                    "Configure your premium experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                if (columnCount > 1) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        maxItemsInEachRow = columnCount
                    ) {
                        val optionModifier = Modifier
                            .weight(1f)
                            .padding(bottom = 16.dp)

                        MoreOption(Icons.Default.Security, "Security Lock", "Protect your data with PIN or Pattern", optionModifier) { onNavigateToSecurity() }
                        MoreOption(Icons.Default.Notifications, "Notifications", "Manage mission alerts & reminders", optionModifier) { onNavigateToNotifications() }
                        MoreOption(Icons.Default.Share, "Share Build Daily", "Invite others to build their future", optionModifier) { onNavigateToShare() }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MoreOption(Icons.Default.Security, "Security Lock", "Protect your data with PIN or Pattern") { onNavigateToSecurity() }
                        MoreOption(Icons.Default.Notifications, "Notifications", "Manage mission alerts & reminders") { onNavigateToNotifications() }
                        MoreOption(Icons.Default.Share, "Share Build Daily", "Invite others to build their future") { onNavigateToShare() }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
