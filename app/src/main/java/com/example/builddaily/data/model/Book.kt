package com.example.builddaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val author: String = "",
    val coverUri: String? = null,
    val price: Double = 0.0,
    val pages: Int = 0,
    val pagesRead: Int = 0,
    val genre: BookGenre = BookGenre.OTHER,
    val priority: BookPriority = BookPriority.MEDIUM,
    val status: BookStatus = BookStatus.WANT_TO_READ,
    val notes: String = "",
    val highlights: String = "",
    val favoriteQuotes: String = "",
    val purchaseLink: String = "",
    val rating: Int = 0,
    val startDate: String? = null,
    val completedDate: String? = null,
    val readingTimeMinutes: Int = 0,
    val createdAt: String = kotlinx.datetime.Clock.System.now().toString(),
    val isFavorite: Boolean = false
)

@Serializable
enum class BookGenre(val displayName: String, val emoji: String) {
    PRODUCTIVITY("Productivity", "🎯"),
    BUSINESS("Business", "💼"),
    FICTION("Fiction", "📖"),
    SELF_HELP("Self-Help", "🌟"),
    TECHNOLOGY("Technology", "💻"),
    PHILOSOPHY("Philosophy", "🤔"),
    BIOGRAPHY("Biography", "👤"),
    SCIENCE("Science", "🔬"),
    PSYCHOLOGY("Psychology", "🧠"),
    FINANCE("Finance", "💰"),
    HEALTH("Health", "❤️"),
    SPIRITUAL("Spiritual", "🕉️"),
    MYSTERY("Mystery", "🔍"),
    ROMANCE("Romance", "💕"),
    OTHER("Other", "📚")
}

@Serializable
enum class BookPriority(val displayName: String, val level: Int, val colorHex: Long) {
    MUST_READ("Must Read", 5, 0xFFFF3B30),
    HIGH_PRIORITY("High Priority", 4, 0xFFFF9500),
    MEDIUM("Medium", 3, 0xFF34C759),
    CASUAL("Casual", 2, 0xFF007AFF),
    SOMEDAY("Someday", 1, 0xFF8E8E93)
}

@Serializable
enum class BookStatus(val displayName: String) {
    CURRENTLY_READING("Currently Reading"),
    WANT_TO_READ("Want to Read"),
    COMPLETED("Completed"),
    ARCHIVED("Archived")
}