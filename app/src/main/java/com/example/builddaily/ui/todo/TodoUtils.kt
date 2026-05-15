package com.example.builddaily.ui.todo

import androidx.compose.ui.graphics.Color
import com.example.builddaily.ui.theme.*
import com.example.builddaily.data.model.TodoGroup
import java.util.*

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

fun getTodoGroup(deadline: Long?): TodoGroup {
    if (deadline == null) return TodoGroup.LATER
    
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = deadline }
    
    if (target.before(now)) return TodoGroup.OVERDUE
    
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }
    if (target.before(today)) return TodoGroup.TODAY
    
    val tomorrow = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }
    if (target.before(tomorrow)) return TodoGroup.TOMORROW
    
    val week = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 7)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }
    if (target.before(week)) return TodoGroup.THIS_WEEK
    
    return TodoGroup.LATER
}

fun getArchiveCategory(completionTime: Long?): String {
    if (completionTime == null) return "Older"
    
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = completionTime }
    
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    if (target.after(today)) return "Today Completed"
    
    val week = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }
    if (target.after(week)) return "This Week"
    
    val month = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }
    if (target.after(month)) return "This Month"
    
    return "Older"
}
