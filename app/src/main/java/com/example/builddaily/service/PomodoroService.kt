package com.example.builddaily.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.builddaily.MainActivity
import com.example.builddaily.data.model.PomodoroSession
import com.example.builddaily.data.repository.PomodoroRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PomodoroService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var initialDurationMinutes: Int = 0

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        const val COMPLETION_CHANNEL_ID = "pomodoro_completion_channel"
        const val NOTIFICATION_ID = 1001
        
        val timeLeft = MutableStateFlow(25 * 60)
        val isRunning = MutableStateFlow(false)
        val currentModeLabel = MutableStateFlow("Focus")
        val isCompleted = MutableStateFlow(false) // New state for UI to show dialog

        fun start(context: Context, durationSeconds: Int, modeLabel: String) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = "START"
                putExtra("DURATION", durationSeconds)
                putExtra("MODE", modeLabel)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = "STOP"
            }
            context.startService(intent)
        }
        
        fun dismissCompletion() {
            isCompleted.value = false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val duration = intent.getIntExtra("DURATION", 25 * 60)
                val mode = intent.getStringExtra("MODE") ?: "Focus"
                initialDurationMinutes = duration / 60
                timeLeft.value = duration
                currentModeLabel.value = mode
                isCompleted.value = false
                startTimer()
            }
            "STOP" -> {
                stopTimer()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        isRunning.value = true
        timerJob?.cancel()
        startForeground(NOTIFICATION_ID, buildNotification())

        val durationSeconds = timeLeft.value
        val endTimeMs = System.currentTimeMillis() + (durationSeconds * 1000L)
        var endingSoonNotified = false

        timerJob = serviceScope.launch {
            while (isRunning.value) {
                val now = System.currentTimeMillis()
                val remainingMs = (endTimeMs - now).coerceAtLeast(0)
                val remainingSec = (remainingMs / 1000).toInt()
                
                if (remainingSec != timeLeft.value) {
                    timeLeft.value = remainingSec
                    updateNotification()
                    
                    // Ending Soon Notification (at 1 minute left)
                    if (remainingSec == 60 && !endingSoonNotified) {
                        showEndingSoonNotification()
                        endingSoonNotified = true
                    }
                }

                if (remainingSec == 0) break
                delay(200) // Fast poll for better UI sync, but accurate math
            }

            if (timeLeft.value == 0 && isRunning.value) {
                isRunning.value = false
                isCompleted.value = true
                
                // Save session stats
                val repo = PomodoroRepository(this@PomodoroService)
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                repo.saveSession(
                    PomodoroSession(
                        mode = currentModeLabel.value,
                        durationMinutes = initialDurationMinutes,
                        timestamp = System.currentTimeMillis(),
                        date = today
                    )
                )

                vibrateStrongly()
                showCompletionNotification()
                // Stop service after notification is shown
                stopForeground(false)
                stopSelf()
            }
        }
    }

    private fun showEndingSoonNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mission Ending Soon")
            .setContentText("Focus period: 1 minute remaining. Finish strong!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }

    private fun stopTimer() {
        isRunning.value = false
        timerJob?.cancel()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val minutes = timeLeft.value / 60
        val seconds = timeLeft.value % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Active")
            .setContentText("$timeStr remaining in ${currentModeLabel.value}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showCompletionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val pendingIntent = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, COMPLETION_CHANNEL_ID)
            .setContentTitle("🏆 SESSION COMPLETE!")
            .setContentText("Your ${currentModeLabel.value} period has ended. Well done!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) // Makes it a heads-up notification
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Standard channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            
            // High importance channel for completion
            val completionChannel = NotificationChannel(
                COMPLETION_CHANNEL_ID,
                "Completion Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                setVibrationPattern(longArrayOf(0, 1000, 200, 1000, 200, 1000))
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(completionChannel)
        }
    }

    private fun vibrateStrongly() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 1000, 200, 1000, 200, 1000, 200, 1000, 200, 1000)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 1000, 200, 1000, 200, 1000, 200, 1000, 200, 1000), -1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}
