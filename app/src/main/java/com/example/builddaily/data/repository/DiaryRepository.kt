package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Diary
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    suspend fun getDiaryByDay(dayId: String): NetworkResult<Diary?>
    suspend fun createDiary(diary: Diary): NetworkResult<Diary>
    suspend fun updateDiary(diary: Diary): NetworkResult<Diary>
    suspend fun deleteDiary(diaryId: String): NetworkResult<Unit>
    fun observeDiaryByDay(dayId: String): Flow<NetworkResult<Diary?>>
}
