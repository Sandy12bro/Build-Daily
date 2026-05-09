package com.example.builddaily.data.repository

import android.content.Context
import com.example.builddaily.data.model.PomodoroSession
import com.example.builddaily.data.model.PomodoroStats
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        
        var newStreak = stats.dayStreak
        if (stats.lastFocusDate.isNotEmpty()) {
            val lastDate = LocalDate.parse(stats.lastFocusDate)
            val currentDate = LocalDate.now()
            
            if (lastDate.plusDays(1) == currentDate) {
                newStreak += 1
            } else if (lastDate != currentDate) {
                newStreak = 1 // Reset if missed a day
            }
        } else {
            newStreak = 1
        }

        val newStats = stats.copy(
            totalFocusMinutes = stats.totalFocusMinutes + session.durationMinutes,
            dayStreak = newStreak,
            lastFocusDate = today
        )
        prefs.edit().putString("stats", json.encodeToString(newStats)).apply()
    }
}
