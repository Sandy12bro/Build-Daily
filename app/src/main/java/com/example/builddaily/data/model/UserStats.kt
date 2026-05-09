package com.example.builddaily.data.model

data class UserStats(
    val totalPoints: Int = 0,
    val totalTasksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val lastCompletionDate: String? = null,
    val firstStartDate: Long = System.currentTimeMillis()
) {
    val daysActive: Int get() {
        val diff = System.currentTimeMillis() - firstStartDate
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
    }

    val growthFactor: Float get() = (daysActive.toFloat() / 90f).coerceIn(0.01f, 1f)

    val level: Int get() = when {
        totalPoints < 50 -> 0
        totalPoints < 200 -> 1
        totalPoints < 500 -> 2
        totalPoints < 1000 -> 3
        else -> 4
    }

    val stageName: String get() = when (level) {
        0 -> "Stardust"
        1 -> "Seed of Life"
        2 -> "Luminescent Sprout"
        3 -> "Crystal Guardian"
        else -> "Zen Life-Form"
    }
}
