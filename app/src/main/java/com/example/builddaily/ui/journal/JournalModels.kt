package com.example.builddaily.ui.journal

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateStr: String, // yyyy-MM-dd
    val mood: String = "calm", // happy, focused, tired, anxious, motivated, calm, stressed
    val moodIntensity: Float = 3f, // 1f to 5f
    val textColorHex: String = "#FFFFFF",
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList(), // work, personal, ideas, goals, memories, learning
    val mediaType: String? = null, // "Image", "Voice", "Drawing", null
    val mediaUrlOrDesc: String? = null,
    val folder: String = "All Notes"
)

@Serializable
data class StickyNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val colorTheme: String = "Purple", // Purple, Cyan, Yellow, Pink, Green
    val isPinned: Boolean = false,
    val type: String = "quick ideas" // quick ideas, reminders, goals, quotes, affirmations, brain dumps
)

@Serializable
data class JournalStats(
    val totalWritten: Int = 0,
    val currentStreak: Int = 0,
    val lastWriteDateStr: String = "",
    val totalWords: Long = 0L,
    val unlockedBadges: List<String> = emptyList()
)

@Serializable
data class MonthlyCoverConfig(
    val monthYearStr: String, // e.g., "May 2026"
    val customTitle: String = "The Evolution Arc",
    val dominantMood: String = "Focused",
    val favoriteMomentSummary: String = "Launching high-impact missions daily."
)
