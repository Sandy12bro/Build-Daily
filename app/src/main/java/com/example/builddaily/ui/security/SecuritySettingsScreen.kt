package com.example.builddaily.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.security.LockType
import com.example.builddaily.data.security.SecuritySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: SecurityViewModel,
    repository: com.example.builddaily.data.security.SecurityRepository,
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security & Privacy", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0118))
            )
        },
        containerColor = Color(0xFF0A0118)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            SecuritySectionTitle("App Lock")
            
            SecurityToggleItem(
                title = "Enable App Lock",
                subtitle = "Require authentication to open Build Daily",
                enabled = settings.isEnabled,
                onToggle = { 
                    if (it) {
                        viewModel.startChangeFlow()
                        onNavigateToSetup()
                    } else {
                        repository.updateSettings(settings.copy(isEnabled = false))
                    }
                }
            )

            if (settings.isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                SecuritySectionTitle("Lock Method")
                
                LockTypeItem("Change Lock Method", false) { 
                    viewModel.startChangeFlow()
                    onNavigateToSetup() 
                }

                Spacer(modifier = Modifier.height(24.dp))
                SecuritySectionTitle("Preferences")

                SecurityToggleItem(
                    title = "Biometric Unlock",
                    subtitle = "Use fingerprint or face recognition",
                    enabled = settings.isBiometricEnabled,
                    onToggle = { repository.updateSettings(settings.copy(isBiometricEnabled = it)) }
                )

                SecurityToggleItem(
                    title = "Screenshot Protection",
                    subtitle = "Block screenshots and screen recording",
                    enabled = settings.isScreenshotBlockingEnabled,
                    onToggle = { repository.updateSettings(settings.copy(isScreenshotBlockingEnabled = it)) }
                )

                SecurityToggleItem(
                    title = "Stealth Mode",
                    subtitle = "Hide app content in recent tasks",
                    enabled = settings.isStealthModeEnabled,
                    onToggle = { repository.updateSettings(settings.copy(isStealthModeEnabled = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Auto-Lock Timeout", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                val timeouts = listOf(0 to "Instantly", 1 to "After 1 minute", 5 to "After 5 minutes", 15 to "After 15 minutes")
                
                timeouts.forEach { (mins, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { repository.updateSettings(settings.copy(autoLockTimeoutMinutes = mins)) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.autoLockTimeoutMinutes == mins,
                            onClick = { repository.updateSettings(settings.copy(autoLockTimeoutMinutes = mins)) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF8B5CF6))
                        )
                        Text(label, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SecuritySectionTitle(title: String) {
    Text(
        title,
        color = Color(0xFF8B5CF6),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SecurityToggleItem(title: String, subtitle: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        PremiumSwitch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun LockTypeItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = if (isSelected) Color(0xFF8B5CF6) else Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6))
        }
    }
}
