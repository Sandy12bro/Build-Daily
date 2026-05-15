package com.example.builddaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.builddaily.ui.theme.BuildDailyTheme

import android.os.Build
import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, kickstart critical systems
            com.example.builddaily.util.HydrationScheduler.kickstart(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Android 12 and below: permissions are granted at install
            com.example.builddaily.util.HydrationScheduler.kickstart(this)
        }

        enableEdgeToEdge()
        setContent {
            BuildDailyTheme {
                BuildDailyApp()
            }
        }
    }
}
