package com.example.builddaily.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Diary(
    val id: String? = null,
    val dayId: String,
    val text: String,
    val mood: Mood,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class Mood {
    EXCELLENT,
    GOOD,
    NEUTRAL,
    BAD,
    TERRIBLE
}
