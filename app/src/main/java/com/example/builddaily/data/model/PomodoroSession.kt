package com.example.builddaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val mode: String, // "Focus", "Break", "Long Break"
    val durationMinutes: Int,
    val timestamp: Long, // epoch millis
    val date: String // yyyy-MM-dd for easy filtering
)

@Serializable
data class PomodoroStats(
    val totalFocusMinutes: Long = 0,
    val dayStreak: Int = 0,
    val lastFocusDate: String = "" // yyyy-MM-dd
)
