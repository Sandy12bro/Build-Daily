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
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            BuildDailyTheme {
                BuildDailyApp()
            }
        }
    }
}
