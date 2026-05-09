package com.example.builddaily.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.builddaily.data.model.Task
import kotlinx.datetime.*
import java.util.*

object TaskScheduler {

    fun scheduleTaskNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val taskDateTime = try {
            val localDate = LocalDate.parse(task.date)
            val localTime = LocalTime.parse(task.startTime)
            LocalDateTime(localDate, localTime).toInstant(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            null
        } ?: return

        val now = Clock.System.now().toEpochMilliseconds()
        val startTime = taskDateTime.toEpochMilliseconds()

        // 1. Start Time Notification
        if (startTime > now) {
            val intent = createIntent(context, task.title, task.description, task.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context, task.id.hashCode(), intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(startTime, pendingIntent), pendingIntent)
        }

        // 2. 10 Minutes Before Notification
        val tenMinBefore = startTime - (10 * 60 * 1000)
        if (tenMinBefore > now) {
            val intent = createIntent(context, "Upcoming Task: ${task.title}", "Starting in 10 minutes", task.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context, task.id.hashCode() + 100000, intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(tenMinBefore, pendingIntent), pendingIntent)
        }
    }

    private fun createIntent(context: Context, title: String, description: String?, id: String): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra("task_title", title)
            putExtra("task_description", description)
            putExtra("task_id", id)
        }
    }

    fun cancelTaskNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        
        // Cancel start time
        PendingIntent.getBroadcast(context, task.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)?.let {
            alarmManager.cancel(it)
        }
        // Cancel 10 min reminder
        PendingIntent.getBroadcast(context, task.id.hashCode() + 100000, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)?.let {
            alarmManager.cancel(it)
        }
    }
}
