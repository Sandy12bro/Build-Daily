package com.example.builddaily.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String? = null,
    val dayId: String,
    val title: String,
    val energyType: EnergyType,
    val time: String? = null,
    val status: TaskStatus,
    val userId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class EnergyType {
    DEEP,
    LIGHT
}

@Serializable
enum class TaskStatus {
    PENDING,
    DONE,
    MISSED
}
