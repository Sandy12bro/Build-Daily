package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Diary
import com.example.builddaily.data.network.NetworkResult
import com.example.builddaily.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.SerializationException

class DiaryRepositoryImpl : DiaryRepository {
    override suspend fun getDiaryByDay(dayId: String): NetworkResult<Diary?> {
        return try {
            NetworkResult.Loading<Diary?>()
            val result = SupabaseClient.client
                .from("diaries")
                .select {
                    filter {
                        eq("day_id", dayId)
                    }
                }
                .decodeSingleOrNull<Diary>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<Diary?>(
                message = "Failed to get diary: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun createDiary(diary: Diary): NetworkResult<Diary> {
        return try {
            NetworkResult.Loading<Diary>()
            val result = SupabaseClient.client
                .from("diaries")
                .insert(diary) {
                    select()
                }
                .decodeSingle<Diary>()
            NetworkResult.Success(result)
        } catch (e: SerializationException) {
            NetworkResult.Error<Diary>(
                message = "Serialization error: ${e.message}",
                exception = e
            )
        } catch (e: Exception) {
            NetworkResult.Error<Diary>(
                message = "Failed to create diary: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun updateDiary(diary: Diary): NetworkResult<Diary> {
        return try {
            NetworkResult.Loading<Diary>()
            val result = SupabaseClient.client
                .from("diaries")
                .update(diary) {
                    select()
                    filter {
                        eq("id", diary.id!!)
                    }
                }
                .decodeSingle<Diary>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<Diary>(
                message = "Failed to update diary: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun deleteDiary(diaryId: String): NetworkResult<Unit> {
        return try {
            NetworkResult.Loading<Unit>()
            SupabaseClient.client
                .from("diaries")
                .delete {
                    filter {
                        eq("id", diaryId)
                    }
                }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error<Unit>(
                message = "Failed to delete diary: ${e.message}",
                exception = e
            )
        }
    }

    override fun observeDiaryByDay(dayId: String): Flow<NetworkResult<Diary?>> = flow {
        emit(NetworkResult.Loading<Diary?>())
        try {
            val result = SupabaseClient.client
                .from("diaries")
                .select {
                    filter {
                        eq("day_id", dayId)
                    }
                }
                .decodeSingleOrNull<Diary>()
            emit(NetworkResult.Success(result))
        } catch (e: Exception) {
            emit(NetworkResult.Error<Diary?>(
                message = "Failed to observe diary: ${e.message}",
                exception = e
            ))
        }
    }.catch { e ->
        emit(NetworkResult.Error<Diary?>(
            message = "Flow error: ${e.message}",
            exception = e
        ))
    }
}
