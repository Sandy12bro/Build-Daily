package com.example.builddaily.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.builddaily.MainActivity
import com.example.builddaily.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val ACTION_LOG_WATER = "com.example.builddaily.ACTION_LOG_WATER"
        const val CHANNEL_ID = "task_reminders"
        const val CHANNEL_NAME = "Task Reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled tasks"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, description: String?, isHydration: Boolean = false) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description ?: "It's time to start your task!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        if (isHydration) {
            val logIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_LOG_WATER
                putExtra("amount", 250)
                putExtra("notification_id", notificationId)
            }
            val logPendingIntent = PendingIntent.getBroadcast(
                context, 1001, logIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(android.R.drawable.ic_menu_add, "Log 250ml", logPendingIntent)
        }

        val notification = builder.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Failed to show notification", e)
        }
    }
}
