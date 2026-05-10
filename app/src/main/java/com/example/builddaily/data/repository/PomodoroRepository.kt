package com.example.builddaily.data.repository

import android.content.Context
import com.example.builddaily.data.model.PomodoroSession
import com.example.builddaily.data.model.PomodoroStats
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

class PomodoroRepository(context: Context) {
    private val prefs = context.getSharedPreferences("pomodoro_stats_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getSessions(): List<PomodoroSession> {
        val sessionsJson = prefs.getString("sessions", "[]") ?: "[]"
        return try {
            json.decodeFromString(sessionsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getStats(): PomodoroStats {
        val statsJson = prefs.getString("stats", "{}") ?: "{}"
        return try {
            json.decodeFromString(statsJson)
        } catch (e: Exception) {
            PomodoroStats()
        }
    }

    fun saveSession(session: PomodoroSession) {
        val sessions = getSessions().toMutableList()
        sessions.add(0, session) // Add to start for history
        // Keep only last 50 sessions for performance
        val trimmedSessions = if (sessions.size > 50) sessions.take(50) else sessions
        prefs.edit().putString("sessions", json.encodeToString(trimmedSessions)).apply()

        if (session.mode == "Focus") {
            updateStats(session)
        }
    }

    private fun updateStats(session: PomodoroSession) {
        val stats = getStats()
        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = currentDateTime.date
        val todayStr = today.toString()
        
        var newStreak = stats.dayStreak
        if (stats.lastFocusDate.isNotEmpty()) {
            try {
                val lastDate = LocalDate.parse(stats.lastFocusDate)
                val diff = lastDate.daysUntil(today)
                
                if (diff == 1) {
                    newStreak += 1
                } else if (diff > 1) {
                    newStreak = 1 // Reset if missed a day
                } else if (diff < 0) {
                    // Date changed backwards or same day? Handle it
                    if (diff < 0) newStreak = 1
                }
            } catch (e: Exception) {
                newStreak = 1
            }
        } else {
            newStreak = 1
        }

        val newStats = stats.copy(
            totalFocusMinutes = stats.totalFocusMinutes + session.durationMinutes,
            dayStreak = newStreak,
            lastFocusDate = todayStr
        )
        prefs.edit().putString("stats", json.encodeToString(newStats)).apply()
    }
}
