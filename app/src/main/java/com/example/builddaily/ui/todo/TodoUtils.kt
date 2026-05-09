package com.example.builddaily.ui.todo

import androidx.compose.ui.graphics.Color
import com.example.builddaily.ui.theme.*

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Work" -> CyberPurple
        "Personal" -> ElectricBlue
        "Health" -> MintGreen
        "Study" -> SolarYellow
        "Finance" -> OceanTeal
        else -> MutedSlate
    }
}
