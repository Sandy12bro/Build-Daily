package com.example.builddaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val category: String = "General",
    val priority: TodoPriority = TodoPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completionTime: Long? = null,
    val deadline: Long? = null,
    val subtasks: List<SubTask> = emptyList(),
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val hasReminder: Boolean = false
)

enum class TodoPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class TodoSortOption {
    DEADLINE, PRIORITY, CREATED_NEWEST, CREATED_OLDEST, ALPHABETICAL, PROGRESS
}

enum class TodoGroup {
    OVERDUE, TODAY, TOMORROW, THIS_WEEK, LATER
}
