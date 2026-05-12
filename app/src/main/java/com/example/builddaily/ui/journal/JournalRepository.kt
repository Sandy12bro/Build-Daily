package com.example.builddaily.ui.journal

import android.content.Context
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JournalRepository(context: Context) {
    private val prefs = context.getSharedPreferences("build_daily_journal_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    // --- Passcode / Lock System ---
    fun getPasscode(): String {
        return prefs.getString("journal_passcode", "") ?: ""
    }

    fun setPasscode(pin: String) {
        prefs.edit().putString("journal_passcode", pin).apply()
    }

    fun isAppLocked(): Boolean {
        return getPasscode().isNotEmpty()
    }

    // --- Journals ---
    fun getJournals(): List<JournalEntry> {
        val jsonStr = prefs.getString("journals", "[]") ?: "[]"
        return try {
            json.decodeFromString(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveJournal(entry: JournalEntry) {
        val existing = getJournals().toMutableList()
        val index = existing.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            existing[index] = entry
        } else {
            existing.add(0, entry)
        }
        prefs.edit().putString("journals", json.encodeToString(existing)).apply()
        updateStatsOnWrite(entry.content)
    }

    fun deleteJournal(id: String) {
        val updated = getJournals().filter { it.id != id }
        prefs.edit().putString("journals", json.encodeToString(updated)).apply()
    }

    // --- Sticky Notes ---
    fun getStickyNotes(): List<StickyNote> {
        val jsonStr = prefs.getString("sticky_notes", "[]") ?: "[]"
        return try {
            json.decodeFromString(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveStickyNote(note: StickyNote) {
        val existing = getStickyNotes().toMutableList()
        val index = existing.indexOfFirst { it.id == note.id }
        if (index != -1) {
            existing[index] = note
        } else {
            existing.add(0, note)
        }
        prefs.edit().putString("sticky_notes", json.encodeToString(existing)).apply()
    }

    fun deleteStickyNote(id: String) {
        val updated = getStickyNotes().filter { it.id != id }
        prefs.edit().putString("sticky_notes", json.encodeToString(updated)).apply()
    }

    // --- Monthly Covers ---
    fun getMonthlyCovers(): Map<String, MonthlyCoverConfig> {
        val jsonStr = prefs.getString("monthly_covers", "{}") ?: "{}"
        return try {
            json.decodeFromString(jsonStr)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveMonthlyCover(config: MonthlyCoverConfig) {
        val map = getMonthlyCovers().toMutableMap()
        map[config.monthYearStr] = config
        prefs.edit().putString("monthly_covers", json.encodeToString(map)).apply()
    }

    // --- Stats & Consistency ---
    fun getStats(): JournalStats {
        val str = prefs.getString("stats", null)
        return if (str != null) {
            try { json.decodeFromString(str) } catch(e:Exception) { JournalStats() }
        } else {
            JournalStats()
        }
    }

    private fun updateStatsOnWrite(content: String) {
        val stats = getStats()
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val todayDate = LocalDate.parse(todayStr)

        var streak = stats.currentStreak
        if (stats.lastWriteDateStr.isNotEmpty()) {
            try {
                val lastDate = LocalDate.parse(stats.lastWriteDateStr)
                val diff = lastDate.daysUntil(todayDate)
                if (diff == 1) {
                    streak += 1
                } else if (diff > 1) {
                    streak = 1
                }
            } catch (e: Exception) {
                if (streak == 0) streak = 1
            }
        } else {
            streak = 1
        }

        val wordCount = content.split("\\s+".toRegex()).count { it.isNotBlank() }.toLong()
        val updatedTotalWords = stats.totalWords + wordCount
        val updatedTotalWritten = stats.totalWritten + 1

        val badges = stats.unlockedBadges.toMutableSet()
        if (streak >= 3) badges.add("Mindful Thinker")
        if (streak >= 7) badges.add("Consistency Scholar")
        if (updatedTotalWritten >= 10) badges.add("Memory Keeper")
        if (updatedTotalWords >= 500) badges.add("Wordsmith Arc")

        val newStats = stats.copy(
            totalWritten = updatedTotalWritten,
            currentStreak = streak,
            lastWriteDateStr = todayStr,
            totalWords = updatedTotalWords,
            unlockedBadges = badges.toList()
        )
        prefs.edit().putString("stats", json.encodeToString(newStats)).apply()
    }
}
