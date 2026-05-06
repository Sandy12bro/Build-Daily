package com.example.builddaily.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String = "",
    @SerialName("device_id") val deviceId: String = "",
    val title: String = "",
    val description: String? = null,
    val date: String = "",
    @SerialName("start_time") val startTime: String = "",
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    val position: Int = 0
)
