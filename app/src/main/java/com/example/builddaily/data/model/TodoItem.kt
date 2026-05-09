package com.example.builddaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val category: String = "General",
    val priority: TodoPriority = TodoPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null
)

enum class TodoPriority {
    LOW, MEDIUM, HIGH
}
