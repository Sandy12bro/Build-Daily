package com.example.builddaily.ui.hydration

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

class HydrationRepository(context: Context) {
    private val prefs = context.getSharedPreferences("hydration_tracker_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getConfig(): HydrationGoalConfig {
        val configJson = prefs.getString("config", null)
        return if (configJson != null) {
            try {
                json.decodeFromString(configJson)
            } catch (e: Exception) {
                HydrationGoalConfig()
            }
        } else {
            HydrationGoalConfig()
        }
    }

    fun saveConfig(config: HydrationGoalConfig) {
        prefs.edit().putString("config", json.encodeToString(config)).apply()
    }

    fun getRecords(): List<HydrationRecord> {
        val recordsJson = prefs.getString("records", "[]") ?: "[]"
        return try {
            json.decodeFromString(recordsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRecord(record: HydrationRecord) {
        val records = getRecords().toMutableList()
        records.add(0, record)
        // Retain last 200 records to ensure premium logging capability without overflow
        val trimmed = if (records.size > 200) records.take(200) else records
        prefs.edit().putString("records", json.encodeToString(trimmed)).apply()
        
        // Update Lifetime stats and evaluate streak/badges if it's water
        if (record.drinkType == "Water") {
            updateStatsOnIntake(record)
        }
    }

    fun deleteRecord(recordId: String) {
        val records = getRecords().filter { it.id != recordId }
        prefs.edit().putString("records", json.encodeToString(records)).apply()
    }

    fun getStats(): HydrationStats {
        val statsJson = prefs.getString("stats", null)
        return if (statsJson != null) {
            try {
                json.decodeFromString(statsJson)
            } catch (e: Exception) {
                HydrationStats()
            }
        } else {
            HydrationStats()
        }
    }

    private fun saveStats(stats: HydrationStats) {
        prefs.edit().putString("stats", json.encodeToString(stats)).apply()
    }

    private fun updateStatsOnIntake(record: HydrationRecord) {
        val stats = getStats()
        val config = getConfig()
        
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val todayDate = LocalDate.parse(todayStr)
        
        var streak = stats.streakDays
        if (stats.lastLogDateStr.isNotEmpty()) {
            try {
                val lastDate = LocalDate.parse(stats.lastLogDateStr)
                val diff = lastDate.daysUntil(todayDate)
                if (diff == 1) {
                    streak += 1
                } else if (diff > 1) {
                    streak = 1 // reset streak if gap > 1 day
                } else if (diff < 0) {
                    streak = 1
                }
                // if diff == 0, keep current streak
            } catch (e: Exception) {
                if (streak == 0) streak = 1
            }
        } else {
            streak = 1
        }

        val updatedLifetimeMl = stats.totalConsumedLifetimeMl + record.amountMl
        
        // Check today's intake to see if perfect day badge should be unlocked
        val allRecords = getRecords()
        val consumedToday = allRecords
            .filter { it.dateStr == todayStr && it.drinkType == "Water" }
            .sumOf { it.amountMl }

        val badges = stats.unlockedBadges.toMutableSet()
        if (consumedToday >= config.calculatedGoalMl) {
            badges.add("Perfect Hydration Day")
        }
        if (streak >= 3) {
            badges.add("Hydration Initiate")
        }
        if (streak >= 7) {
            badges.add("7 Day Hydration Streak")
        }
        if (updatedLifetimeMl >= 25000L) {
            badges.add("Hydration Master")
        }

        saveStats(
            stats.copy(
                streakDays = streak,
                lastLogDateStr = todayStr,
                totalConsumedLifetimeMl = updatedLifetimeMl,
                unlockedBadges = badges.toList()
            )
        )
    }
}
