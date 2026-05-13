package com.example.builddaily.data.model

data class UserStats(
    val totalPoints: Int = 0,
    val totalTasksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastCompletionDate: String? = null,
    val firstStartDate: Long = System.currentTimeMillis()
) {
    val effectiveCurrentStreak: Int get() = currentStreak

    // Connect the tree's growth stages directly to the real-time verified effective streak!
    val daysActive: Int get() = effectiveCurrentStreak.coerceAtLeast(1)

    // Smooth growth multiplier based on real-time streak progression
    val growthFactor: Float get() = (effectiveCurrentStreak.toFloat() / 30f).coerceIn(0.01f, 1f)

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
