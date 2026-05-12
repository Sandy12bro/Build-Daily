package com.example.builddaily.ui.hydration

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class HydrationRecord(
    val id: String = UUID.randomUUID().toString(),
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateStr: String, // yyyy-MM-dd
    val drinkType: String = "Water" // "Water", "Caffeine"
)

@Serializable
data class HydrationGoalConfig(
    val weightKg: Int = 70,
    val gender: String = "Male", // "Male", "Female", "Other"
    val age: Int = 25,
    val activityLevel: String = "Moderate", // "Low", "Moderate", "High"
    val weatherTemp: String = "Normal", // "Cool", "Normal", "Hot"
    val customGoalMl: Int? = null,
    val reminderIntervalMins: Int = 60,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val bedtimeReduction: Boolean = true,
    val reminderSound: String = "Droplet Sound"
) {
    val calculatedGoalMl: Int
        get() {
            if (customGoalMl != null) return customGoalMl
            // Base calculation: 35ml per kg of body weight
            var baseMl = weightKg * 35
            
            // Adjustments
            if (activityLevel == "High") baseMl += 500
            else if (activityLevel == "Low") baseMl -= 200

            if (weatherTemp == "Hot") baseMl += 400
            else if (weatherTemp == "Cool") baseMl -= 200

            return baseMl.coerceIn(1500, 5000)
        }
}

@Serializable
data class HydrationStats(
    val streakDays: Int = 0,
    val lastLogDateStr: String = "",
    val totalConsumedLifetimeMl: Long = 0L,
    val unlockedBadges: List<String> = emptyList()
)
