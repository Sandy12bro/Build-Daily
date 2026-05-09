package com.example.builddaily.ui.splash

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.SpaceBlack
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Alpha"
    )
    
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "Scale"
    )

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)
        
        // Request Notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Check for Exact Alarm permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
        
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { clip = false }
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            Text(
                text = " 💪🏻 ",
                fontSize = 80.sp,
                modifier = Modifier
                    .graphicsLayer { clip = false }
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "BUILD DAILY",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(CyberPurple, ElectricBlue)
                    )
                )
            )
            
            Text(
                text = """
                    Either push your limits 
                                    or 
                    suffocate in your comfort zone
                """.trimIndent(),
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
        }
    }
}
