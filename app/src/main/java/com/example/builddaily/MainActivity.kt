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

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import android.view.WindowManager
import com.example.builddaily.data.security.SecurityRepository

class MainActivity : FragmentActivity() {

    private lateinit var securityRepository: SecurityRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            com.example.builddaily.util.HydrationScheduler.kickstart(this)
            com.example.builddaily.util.HydrationWatchdogWorker.schedule(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        securityRepository = SecurityRepository(this)

        // Observe App Lifecycle for Auto-Lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                // Check if app needs locking when returning to foreground
                applySecurityFlags()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            com.example.builddaily.util.HydrationScheduler.kickstart(this)
            com.example.builddaily.util.HydrationWatchdogWorker.schedule(this)
        }

        enableEdgeToEdge()
        applySecurityFlags()

        setContent {
            BuildDailyTheme {
                BuildDailyApp(securityRepository)
            }
        }
    }

    private fun applySecurityFlags() {
        val settings = securityRepository.settings.value
        if (settings.isEnabled && settings.isScreenshotBlockingEnabled) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
