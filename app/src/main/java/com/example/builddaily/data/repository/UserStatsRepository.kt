package com.example.builddaily.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.builddaily.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.builddaily.util.today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class UserStatsRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_stats", Context.MODE_PRIVATE)
    
    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    private fun loadStats(): UserStats {
        val firstStart = prefs.getLong("first_start", System.currentTimeMillis())
        if (!prefs.contains("first_start")) {
            prefs.edit().putLong("first_start", firstStart).apply()
        }
        
        var savedStreak = prefs.getInt("current_streak", 0)
        val savedMaxStreak = prefs.getInt("max_streak", 0)
        val lastDateStr = prefs.getString("last_date", null)
        
        // Auto-refresh/reset if the user missed yesterday or more days!
        if (lastDateStr != null) {
            try {
                val cleanDate = if (lastDateStr.length >= 10) lastDateStr.substring(0, 10) else lastDateStr
                val lastDays = kotlinx.datetime.LocalDate.parse(cleanDate).toEpochDays()
                val todayDays = today().toEpochDays()
                if (todayDays - lastDays > 1) {
                    // Criteria not met yesterday, streak restarts/resets to 0
                    savedStreak = 0
                    prefs.edit().putInt("current_streak", 0).apply()
                }
            } catch(e: Exception) {}
        }
        
        return UserStats(
            totalPoints = prefs.getInt("total_points", 0),
            totalTasksCompleted = prefs.getInt("total_tasks", 0),
            currentStreak = savedStreak,
            maxStreak = maxOf(savedMaxStreak, savedStreak),
            lastCompletionDate = lastDateStr,
            firstStartDate = firstStart
        )
    }

    fun addPoints(points: Int, isCriteriaMet: Boolean = false) {
        val current = _stats.value
        val newPoints = current.totalPoints + points
        val newTotalTasks = current.totalTasksCompleted + 1

        // IMPORTANT: Do NOT touch streak here.
        // Streak is calculated ONLY by recalculateStreak() which uses strict consecutive-day logic.
        // addPoints only handles XP and task count.
        android.util.Log.d("StreakEngine", "addPoints: +$points XP, criteriaMet=$isCriteriaMet (streak untouched, handled by recalculateStreak)")

        val updated = current.copy(
            totalPoints = newPoints,
            totalTasksCompleted = newTotalTasks
        )

        saveStats(updated)
        _stats.value = updated
    }

    fun awardXP(points: Int) {
        val current = _stats.value
        val updated = current.copy(totalPoints = current.totalPoints + points)
        saveStats(updated)
        _stats.value = updated
    }

    suspend fun recalculateStreak(taskRepository: TaskRepository) {
        val todayDate = today()
        val todayStr = todayDate.toString()
        val pastDate = todayDate.minus(365, kotlinx.datetime.DateTimeUnit.DAY).toString()

        try {
            val tasks = taskRepository.getTasksInRange(pastDate, todayStr)
            android.util.Log.d("StreakEngine", "===========================================================")
            android.util.Log.d("StreakEngine", "========== NEW STREAK RECALCULATION (STRICT CONSECUTIVE) ==========")
            android.util.Log.d("StreakEngine", "Today: $todayStr, Range: $pastDate to $todayStr")
            android.util.Log.d("StreakEngine", "Total tasks loaded: ${tasks.size}")

            val tasksByDate = tasks.groupBy { it.date }
            android.util.Log.d("StreakEngine", "Unique dates in data: ${tasksByDate.keys.sorted().take(10)}")

            var maxStreak = 0
            var calculatedCurrentStreak = 0

            android.util.Log.d("StreakEngine", "===========================================================")
            android.util.Log.d("StreakEngine", "SECTION 1: CALCULATE HISTORICAL MAX STREAK (forward scan)")
            android.util.Log.d("StreakEngine", "===========================================================")

            if (tasksByDate.isNotEmpty()) {
                val sortedDates = tasksByDate.keys.sorted()
                val minDateStr = sortedDates.first()
                val maxDateStr = sortedDates.last()
                var currentDate = kotlinx.datetime.LocalDate.parse(minDateStr)
                val endDate = todayDate
                var tempStreak = 0
                var streakBrokenAt: String? = null

                android.util.Log.d("StreakEngine", "Scanning from $minDateStr to $endDate")

                while (currentDate <= endDate) {
                    val dateStr = currentDate.toString()
                    val dayTasks = tasksByDate[dateStr]

                    if (dayTasks == null || dayTasks.isEmpty()) {
                        if (tempStreak > 0) {
                            android.util.Log.d("StreakEngine", "  [$dateStr] NO TASKS → STREAK BROKEN (was $tempStreak)")
                            streakBrokenAt = dateStr
                        }
                        tempStreak = 0
                    } else {
                        val completed = dayTasks.count { it.isCompleted }
                        val total = dayTasks.size
                        val percentage = completed.toFloat() / total

                        if (percentage >= 0.75f) {
                            tempStreak++
                            if (tempStreak == 1) {
                                android.util.Log.d("StreakEngine", "  [$dateStr] STREAK START: $completed/$total (${(percentage*100).toInt()}%)")
                            } else {
                                android.util.Log.d("StreakEngine", "  [$dateStr] VALID: $completed/$total (${(percentage*100).toInt()}%) → streak=$tempStreak")
                            }
                            maxStreak = maxOf(maxStreak, tempStreak)
                        } else {
                            if (tempStreak > 0) {
                                android.util.Log.d("StreakEngine", "  [$dateStr] FAILED: $completed/$total (${(percentage*100).toInt()}%) < 75% → STREAK BROKEN at $tempStreak")
                                streakBrokenAt = dateStr
                            }
                            tempStreak = 0
                        }
                    }
                    currentDate = currentDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
                }
                android.util.Log.d("StreakEngine", "Historical max streak found: $maxStreak")
            }

            android.util.Log.d("StreakEngine", "===========================================================")
            android.util.Log.d("StreakEngine", "SECTION 2: CALCULATE CURRENT CONSECUTIVE STREAK (backwards from TODAY)")
            android.util.Log.d("StreakEngine", "===========================================================")

            var currentBackDate = todayDate
            var daysChecked = 0

            android.util.Log.d("StreakEngine", "Starting backwards scan from TODAY ($todayDate)")

            while (daysChecked < 365) {
                val dateStr = currentBackDate.toString()
                val dayTasks = tasksByDate[dateStr] ?: emptyList()

                val completed = dayTasks.count { it.isCompleted }
                val total = dayTasks.size
                val percentage = if (total > 0) completed.toFloat() / total else 0f

                if (percentage >= 0.75f) {
                    calculatedCurrentStreak++
                } else {
                    // If today is not yet complete (or has no tasks), don't break the streak yet.
                    // A streak is only officially broken if YESTERDAY or any previous day was missed.
                    if (currentBackDate == todayDate) {
                        android.util.Log.d("StreakEngine", "    → Today ($dateStr) not yet complete (rate: ${(percentage*100).toInt()}%), continuing to check yesterday...")
                    } else {
                        android.util.Log.d("StreakEngine", "    → Missed goal on $dateStr (rate: ${(percentage*100).toInt()}%) → BREAK LOOP")
                        break
                    }
                }

                currentBackDate = currentBackDate.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
                daysChecked++
            }

            android.util.Log.d("StreakEngine", "Final consecutive streak from today: $calculatedCurrentStreak")

            val current = _stats.value
            val newMaxStreak = maxOf(current.maxStreak, maxStreak)
            val newLastDate = if (calculatedCurrentStreak > 0) todayStr else current.lastCompletionDate

            val updated = current.copy(
                currentStreak = calculatedCurrentStreak,
                maxStreak = newMaxStreak,
                lastCompletionDate = newLastDate
            )

            android.util.Log.d("StreakEngine", "========== FINAL RESULT ==========")
            android.util.Log.d("StreakEngine", "Current Consecutive Streak: $calculatedCurrentStreak")
            android.util.Log.d("StreakEngine", "Historical Max Streak: $newMaxStreak")
            android.util.Log.d("StreakEngine", "Last Completion Date: $newLastDate")
            android.util.Log.d("StreakEngine", "==================================")

            saveStats(updated)
            _stats.value = updated
        } catch (e: Exception) {
            android.util.Log.e("StreakEngine", "Error recalculating streak: ${e.message}", e)
        }
    }

    private fun saveStats(stats: UserStats) {
        prefs.edit().apply {
            putInt("total_points", stats.totalPoints)
            putInt("total_tasks", stats.totalTasksCompleted)
            putInt("current_streak", stats.currentStreak)
            putInt("max_streak", stats.maxStreak)
            putString("last_date", stats.lastCompletionDate)
            putLong("first_start", stats.firstStartDate)
            apply()
        }
    }

    fun resetStats() {
        prefs.edit().clear().apply()
        val fresh = UserStats()
        _stats.value = fresh
    }
}
