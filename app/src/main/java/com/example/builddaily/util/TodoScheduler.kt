package com.example.builddaily.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.builddaily.data.model.TodoItem

object TodoScheduler {

    fun scheduleTodoReminder(context: Context, todo: TodoItem) {
        if (todo.isCompleted) {
            cancelTodoReminder(context, todo.id)
            return
        }

        val deadline = todo.deadline ?: return
        val now = System.currentTimeMillis()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. 24 Hours Before
        val trigger24h = deadline - (24 * 60 * 60 * 1000)
        if (trigger24h > now) {
            scheduleAlarm(context, alarmManager, trigger24h, todo.id.hashCode() + 10, todo.title, "Deadline in 24 hours!")
        }

        // 2. 1 Hour Before
        val trigger1h = deadline - (60 * 60 * 1000)
        if (trigger1h > now) {
            scheduleAlarm(context, alarmManager, trigger1h, todo.id.hashCode() + 20, todo.title, "Deadline in 1 hour!")
        }

        // 3. Exactly at Deadline
        if (deadline > now) {
            scheduleAlarm(context, alarmManager, deadline, todo.id.hashCode() + 30, todo.title, "Deadline reached! Mission ending.")
        }
    }

    private fun scheduleAlarm(context: Context, am: AlarmManager, time: Long, reqCode: Int, title: String, desc: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("task_title", title)
            putExtra("task_description", desc)
            putExtra("task_id", "TODO_$reqCode")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val info = AlarmManager.AlarmClockInfo(time, pendingIntent)
        am.setAlarmClock(info, pendingIntent)
    }

    fun cancelTodoReminder(context: Context, todoId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        
        listOf(10, 20, 30).forEach { offset ->
            val reqCode = todoId.hashCode() + offset
            PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )?.let {
                alarmManager.cancel(it)
            }
        }
    }
}
