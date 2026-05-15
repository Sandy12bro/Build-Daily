package com.example.builddaily.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.builddaily.ui.hydration.HydrationGoalConfig
import com.example.builddaily.ui.hydration.HydrationRepository
import kotlinx.datetime.*
import java.util.*

object HydrationScheduler {
    private const val REQUEST_CODE = 999999
    private const val PREFS_NAME = "hydration_scheduler_prefs"
    private const val KEY_NEXT_ALARM = "next_alarm_time"

    fun getNextAlarmTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_NEXT_ALARM, 0L)
    }

    fun kickstart(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val nextAlarm = prefs.getLong(KEY_NEXT_ALARM, 0L)
        val now = Clock.System.now().toEpochMilliseconds()

        // If no alarm is set or the old one is in the past, schedule a fresh one
        if (nextAlarm == 0L || nextAlarm < now) {
            reschedule(context)
        }
    }

    fun reschedule(context: Context) {
        val repo = HydrationRepository(context)
        scheduleNext(context, repo.getConfig())
    }

    fun scheduleNext(context: Context, config: HydrationGoalConfig) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Clock.System.now()
        val nextTrigger = calculateNextTrigger(now, config)
        val triggerMs = nextTrigger.toEpochMilliseconds()

        // Store next alarm time to prevent duplicate resets on every app open
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_NEXT_ALARM, triggerMs).apply()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("task_title", "Hydration Mission")
            putExtra("task_description", "Time to hydrate! Keep your wellness peak high 💧")
            putExtra("task_id", "HYDRATION_REMINDER")
            putExtra("is_hydration", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fallback for missing permission
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        } else {
            // High precision
            val info = AlarmManager.AlarmClockInfo(triggerMs, pendingIntent)
            alarmManager.setAlarmClock(info, pendingIntent)
        }
    }

    private fun calculateNextTrigger(now: Instant, config: HydrationGoalConfig): Instant {
        val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val intervalMillis = config.reminderIntervalMins * 60 * 1000L
        var trigger = now.plus(DateTimePeriod(minutes = config.reminderIntervalMins), TimeZone.currentSystemDefault())

        // Check if trigger falls in quiet hours
        if (isInQuietHours(trigger, config)) {
            // Move to end of quiet hours (next day start)
            trigger = getQuietHoursEndInstant(trigger, config)
        }

        return trigger
    }

    private fun isInQuietHours(instant: Instant, config: HydrationGoalConfig): Boolean {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val currentStr = String.format("%02d:%02d", local.hour, local.minute)
        
        return if (config.quietHoursStart > config.quietHoursEnd) {
            // Overnights (e.g., 22:00 - 07:00)
            currentStr >= config.quietHoursStart || currentStr < config.quietHoursEnd
        } else {
            currentStr in config.quietHoursStart..config.quietHoursEnd
        }
    }

    private fun getQuietHoursEndInstant(instant: Instant, config: HydrationGoalConfig): Instant {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val endParts = config.quietHoursEnd.split(":")
        val endHour = endParts[0].toInt()
        val endMin = endParts[1].toInt()

        var nextDay = local.date
        if (local.hour >= endHour) {
            nextDay = nextDay.plus(1, DateTimeUnit.DAY)
        }

        val nextTime = LocalDateTime(nextDay, LocalTime(endHour, endMin))
        return nextTime.toInstant(TimeZone.currentSystemDefault())
    }
}
