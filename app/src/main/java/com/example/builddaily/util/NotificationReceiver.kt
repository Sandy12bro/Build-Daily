package com.example.builddaily.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("task_title") ?: "Task Reminder"
        val description = intent.getStringExtra("task_description")
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, description)
        
        // Log to history
        NotificationHistoryManager.addToHistory(context, title, description)
    }
}
