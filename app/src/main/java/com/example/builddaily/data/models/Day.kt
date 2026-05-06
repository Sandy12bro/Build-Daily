package com.example.builddaily.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Day(
    val id: String? = null,
    val userId: String,
    val date: String, // Format: "yyyy-MM-dd"
    val completionRate: Float = 0.0f, // 0.0 to 1.0
    val createdAt: String? = null,
    val updatedAt: String? = null
)
