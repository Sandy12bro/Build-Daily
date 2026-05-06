package com.example.builddaily.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String? = null,
    val dayId: String,
    val title: String,
    val energyType: EnergyType,
    val status: TaskStatus,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class EnergyType {
    HIGH,
    MEDIUM,
    LOW
}

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}
