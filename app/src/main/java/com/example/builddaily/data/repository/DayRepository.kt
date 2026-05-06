package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Day
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface DayRepository {
    suspend fun getDayByDate(date: String): NetworkResult<Day?>
    suspend fun createDay(day: Day): NetworkResult<Day>
    suspend fun updateDay(day: Day): NetworkResult<Day>
    suspend fun deleteDay(dayId: String): NetworkResult<Unit>
    fun observeDay(date: String): Flow<NetworkResult<Day?>>
}
