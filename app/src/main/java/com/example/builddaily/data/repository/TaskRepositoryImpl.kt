package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Task
import com.example.builddaily.data.network.NetworkResult
import com.example.builddaily.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.SerializationException

class TaskRepositoryImpl : TaskRepository {
    override suspend fun getTasksByDay(dayId: String): NetworkResult<List<Task>> {
        return try {
            NetworkResult.Loading<List<Task>>()
            val result = SupabaseClient.client
                .from("tasks")
                .select {
                    filter {
                        eq("day_id", dayId)
                    }
                }
                .decodeList<Task>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<List<Task>>(
                message = "Failed to get tasks: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun createTask(task: Task): NetworkResult<Task> {
        return try {
            NetworkResult.Loading<Task>()
            val result = SupabaseClient.client
                .from("tasks")
                .insert(task) {
                    select()
                }
                .decodeSingle<Task>()
            NetworkResult.Success(result)
        } catch (e: SerializationException) {
            NetworkResult.Error<Task>(
                message = "Serialization error: ${e.message}",
                exception = e
            )
        } catch (e: Exception) {
            NetworkResult.Error<Task>(
                message = "Failed to create task: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun updateTask(task: Task): NetworkResult<Task> {
        return try {
            NetworkResult.Loading<Task>()
            val result = SupabaseClient.client
                .from("tasks")
                .update(task) {
                    select()
                    filter {
                        eq("id", task.id!!)
                    }
                }
                .decodeSingle<Task>()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error<Task>(
                message = "Failed to update task: ${e.message}",
                exception = e
            )
        }
    }

    override suspend fun deleteTask(taskId: String): NetworkResult<Unit> {
        return try {
            NetworkResult.Loading<Unit>()
            SupabaseClient.client
                .from("tasks")
                .delete {
                    filter {
                        eq("id", taskId)
                    }
                }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error<Unit>(
                message = "Failed to delete task: ${e.message}",
                exception = e
            )
        }
    }

    override fun observeTasksByDay(dayId: String): Flow<NetworkResult<List<Task>>> = flow {
        emit(NetworkResult.Loading<List<Task>>())
        try {
            val result = SupabaseClient.client
                .from("tasks")
                .select {
                    filter {
                        eq("day_id", dayId)
                    }
                }
                .decodeList<Task>()
            emit(NetworkResult.Success(result))
        } catch (e: Exception) {
            emit(NetworkResult.Error<List<Task>>(
                message = "Failed to observe tasks: ${e.message}",
                exception = e
            ))
        }
    }.catch { e ->
        emit(NetworkResult.Error<List<Task>>(
            message = "Flow error: ${e.message}",
            exception = e
        ))
    }
}
