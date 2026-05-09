package com.example.builddaily.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.builddaily.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.builddaily.util.today

class UserStatsRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_stats", Context.MODE_PRIVATE)
    
    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    private fun loadStats(): UserStats {
        val firstStart = prefs.getLong("first_start", System.currentTimeMillis())
        // Save it immediately if it's new
        if (!prefs.contains("first_start")) {
            prefs.edit().putLong("first_start", firstStart).apply()
        }
        
        return UserStats(
            totalPoints = prefs.getInt("total_points", 0),
            totalTasksCompleted = prefs.getInt("total_tasks", 0),
            currentStreak = prefs.getInt("current_streak", 0),
            lastCompletionDate = prefs.getString("last_date", null),
            firstStartDate = firstStart
        )
    }

    fun addPoints(points: Int) {
        val current = _stats.value
        val newPoints = current.totalPoints + points
        val newTotalTasks = current.totalTasksCompleted + 1
        
        val todayStr = today().toString()
        var newStreak = current.currentStreak
        
        if (current.lastCompletionDate != todayStr) {
            // Check if yesterday was the last completion for streak
            // For now, just increment if it's a new day
            newStreak++
        }

        val updated = current.copy(
            totalPoints = newPoints,
            totalTasksCompleted = newTotalTasks,
            currentStreak = newStreak,
            lastCompletionDate = todayStr
        )
        
        saveStats(updated)
        _stats.value = updated
    }

    private fun saveStats(stats: UserStats) {
        prefs.edit().apply {
            putInt("total_points", stats.totalPoints)
            putInt("total_tasks", stats.totalTasksCompleted)
            putInt("current_streak", stats.currentStreak)
            putString("last_date", stats.lastCompletionDate)
            putLong("first_start", stats.firstStartDate)
            apply()
        }
    }
}
