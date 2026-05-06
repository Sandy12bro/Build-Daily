package com.example.builddaily.data.repository

import com.example.builddaily.data.models.Task
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun getTasksByDay(dayId: String, userId: String): NetworkResult<List<Task>>
    suspend fun createTask(task: Task): NetworkResult<Task>
    suspend fun updateTask(task: Task): NetworkResult<Task>
    suspend fun deleteTask(taskId: String, userId: String): NetworkResult<Unit>
    fun observeTasksByDay(dayId: String, userId: String): Flow<NetworkResult<List<Task>>>
}
