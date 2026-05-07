package com.example.builddaily.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

fun today(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDate.formatDisplay(): String {
    val javaDate = java.time.LocalDate.of(this.year, this.monthNumber, this.dayOfMonth)
    val dayOfWeek = javaDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val month = javaDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$dayOfWeek, ${this.dayOfMonth} $month ${this.year}"
}

fun LocalDate.toEpochMillis(): Long {
    val javaDate = java.time.LocalDate.of(this.year, this.monthNumber, this.dayOfMonth)
    return javaDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
}

fun fromEpochMillis(millis: Long): LocalDate {
    val instant = java.time.Instant.ofEpochMilli(millis)
    val javaDate = instant.atZone(java.time.ZoneId.of("UTC")).toLocalDate()
    return LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
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
