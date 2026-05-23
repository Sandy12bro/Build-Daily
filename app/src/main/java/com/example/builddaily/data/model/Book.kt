package com.example.builddaily.data.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock

@Serializable
data class Book(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val author: String = "",
    val coverUri: String? = null,
    val totalPages: Int = 0,
    val pagesRead: Int = 0,
    val price: Double = 0.0,
    val genre: String = "Other",
    val language: String = "English",
    val priority: BookPriority = BookPriority.MEDIUM,
    val status: ReadingStatus = ReadingStatus.WANT,
    val notes: String = "",
    val link: String = "",
    val tags: List<String> = emptyList(),
    val startDate: String? = null,
    val completedDate: String? = null,
    val lastUpdated: String = Clock.System.now().toString(),
    val createdAt: String = Clock.System.now().toString(),
    val isFavorite: Boolean = false
) {
    val progress: Float
        get() = if (totalPages > 0) (pagesRead.toFloat() / totalPages).coerceIn(0f, 1f) else 0f
        
    val percentage: Int
        get() = (progress * 100).toInt()
        
    val remainingPages: Int
        get() = (totalPages - pagesRead).coerceAtLeast(0)
}

@Serializable
enum class BookPriority(val displayName: String, val level: Int, val colorHex: Long) {
    HIGH("High", 3, 0xFFFF3B30),
    MEDIUM("Medium", 2, 0xFF34C759),
    LOW("Low", 1, 0xFF007AFF)
}

@Serializable
enum class ReadingStatus(val displayName: String) {
    READING("Reading"),
    WANT("Want to Read"),
    DONE("Done")
}