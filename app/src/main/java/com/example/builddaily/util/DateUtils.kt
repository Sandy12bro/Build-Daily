package com.example.builddaily.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.*

fun today(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDate.formatDisplay(): String {
    val calendar = Calendar.getInstance()
    calendar.set(this.year, this.monthNumber - 1, this.dayOfMonth)
    val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}

fun LocalDate.toEpochMillis(): Long {
    val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.set(this.year, this.monthNumber - 1, this.dayOfMonth, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun fromEpochMillis(millis: Long): LocalDate {
    val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis
    return LocalDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun formatTime(time: String): String {
    val parts = time.split(":")
    if (parts.size < 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1]
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:$minute $amPm"
}
