package com.example.builddaily.ui.profile

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
            }
        }
    }
}
