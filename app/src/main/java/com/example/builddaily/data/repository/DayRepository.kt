package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Day
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface DayRepository {
    suspend fun getDayByDate(date: String, userId: String): NetworkResult<Day?>
    suspend fun createDay(day: Day): NetworkResult<Day>
    suspend fun updateDay(day: Day): NetworkResult<Day>
    suspend fun deleteDay(dayId: String, userId: String): NetworkResult<Unit>
    fun observeDay(date: String, userId: String): Flow<NetworkResult<Day?>>
}
