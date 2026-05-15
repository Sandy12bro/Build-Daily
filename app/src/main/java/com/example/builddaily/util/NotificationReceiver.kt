package com.example.builddaily.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Clock

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_LOG_WATER) {
            val amount = intent.getIntExtra("amount", 250)
            val repo = com.example.builddaily.ui.hydration.HydrationRepository(context)
            val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val record = com.example.builddaily.ui.hydration.HydrationRecord(
                amountMl = amount,
                dateStr = todayStr,
                drinkType = "Water"
            )
            repo.saveRecord(record)
            
            // Reschedule next since user just drank
            HydrationScheduler.reschedule(context)
            
            // Cancel current notification (optional, but good UX)
            // Cancel only this notification
            val notificationId = intent.getIntExtra("notification_id", -1)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            if (notificationId != -1) {
                notificationManager.cancel(notificationId)
            }
            return
        }

        val title = intent.getStringExtra("task_title") ?: "Task Reminder"
        val description = intent.getStringExtra("task_description")
        val isHydration = intent.getBooleanExtra("is_hydration", false)
        
        android.util.Log.d("NotificationReceiver", "Alarm triggered for: $title")

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, description, isHydration)
        
        // Daisy-chain hydration reminders
        if (isHydration) {
            HydrationScheduler.reschedule(context)
        }

        // Log to history
        NotificationHistoryManager.addToHistory(context, title, description)
    }
}
