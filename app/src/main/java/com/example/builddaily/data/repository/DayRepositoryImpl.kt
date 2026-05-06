package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Day
import com.example.builddaily.data.network.NetworkResult
import com.example.builddaily.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.SerializationException

class DayRepositoryImpl : DayRepository {
    override suspend fun getDayByDate(date: String, userId: String): NetworkResult<Day?> {
        return try {
            NetworkResult.Loading<Day?>()
            val result = SupabaseClient.client
                .from("days")
                .select {
                    filter {
                        eq("date", date)
                        eq("userId", userId)
                    }
                }
                .decodeSingleOrNull<Day>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<Day?>(
                message = "Failed to get day: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun createDay(day: Day): NetworkResult<Day> {
        return try {
            NetworkResult.Loading<Day>()
            val result = SupabaseClient.client
                .from("days")
                .insert(day) {
                    select()
                }
                .decodeSingle<Day>()
            NetworkResult.Success(result)
        } catch (e: SerializationException) {
            NetworkResult.Error<Day>(
                message = "Serialization error: ${e.message}",
                exception = e
            )
        } catch (e: Exception) {
            NetworkResult.Error<Day>(
                message = "Failed to create day: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun updateDay(day: Day): NetworkResult<Day> {
        return try {
            NetworkResult.Loading<Day>()
            val result = SupabaseClient.client
                .from("days")
                .update(day) {
                    select()
                    filter {
                        eq("id", day.id!!)
                    }
                }
                .decodeSingle<Day>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<Day>(
                message = "Failed to update day: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun deleteDay(dayId: String, userId: String): NetworkResult<Unit> {
        return try {
            NetworkResult.Loading<Unit>()
            SupabaseClient.client
                .from("days")
                .delete {
                    filter {
                        eq("id", dayId)
                        eq("userId", userId)
                    }
                }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error<Unit>(
                message = "Failed to delete day: ${e.message}",
                exception = e
            )
        }
    }

    override fun observeDay(date: String, userId: String): Flow<NetworkResult<Day?>> = flow {
        emit(NetworkResult.Loading<Day?>())
        try {
            val result = SupabaseClient.client
                .from("days")
                .select {
                    filter {
                        eq("date", date)
                        eq("userId", userId)
                    }
                }
                .decodeSingleOrNull<Day>()
            emit(NetworkResult.Success(result))
        } catch (e: Exception) {
            emit(NetworkResult.Error<Day?>(
                message = "Failed to observe day: ${e.message}",
                exception = e
            ))
        }
    }.catch { e ->
        emit(NetworkResult.Error<Day?>(
            message = "Flow error: ${e.message}",
            exception = e
        ))
    }

    override suspend fun getAllDays(userId: String): NetworkResult<List<Day>> {
        return try {
            NetworkResult.Loading<List<Day>>()
            val result = SupabaseClient.client
                .from("days")
                .select {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Day>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<List<Day>>(
                message = "Failed to get all days: ${e.message}",
                exception = e
            )
        }
    }
}
