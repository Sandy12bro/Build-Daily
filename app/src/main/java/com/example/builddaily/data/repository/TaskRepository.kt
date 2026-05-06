package com.example.builddaily.data.repository

import com.example.builddaily.data.SupabaseClient
import com.example.builddaily.data.model.Task
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class TaskRepository(private val deviceId: String) {

    private val table = "tasks"
    private val isDemoMode = SupabaseClient.client.supabaseUrl.contains("your-project.supabase.co") || SupabaseClient.client.supabaseUrl.isBlank()

    companion object {
        private val mockTasks = mutableListOf<Task>()
    }

    suspend fun getTasksForDate(date: String): List<Task> {
        if (isDemoMode) {
            return mockTasks.filter { it.date == date }.sortedBy { it.startTime }
        }
        return SupabaseClient.client.from(table)
            .select {
                filter {
                    eq("device_id", deviceId)
                    eq("date", date)
                }
                order("start_time", Order.ASCENDING)
            }
            .decodeList<Task>()
    }

    suspend fun insertTask(task: Task): Task {
        if (isDemoMode) {
            val newTask = task.copy(id = java.util.UUID.randomUUID().toString())
            mockTasks.add(newTask)
            return newTask
        }
        return SupabaseClient.client.from(table)
            .insert(task) { select() }
            .decodeSingle<Task>()
    }

    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        if (isDemoMode) {
            val index = mockTasks.indexOfFirst { it.id == taskId }
            if (index != -1) {
                mockTasks[index] = mockTasks[index].copy(isCompleted = isCompleted)
            }
            return
        }
        SupabaseClient.client.from(table)
            .update({ set("is_completed", isCompleted) }) {
                filter { eq("id", taskId) }
            }
    }

    suspend fun deleteTask(taskId: String) {
        if (isDemoMode) {
            mockTasks.removeAll { it.id == taskId }
            return
        }
        SupabaseClient.client.from(table)
            .delete {
                filter { eq("id", taskId) }
            }
    }

    suspend fun getTasksInRange(startDate: String, endDate: String): List<Task> {
        if (isDemoMode) {
            return mockTasks.filter { it.date >= startDate && it.date <= endDate }
        }
        return SupabaseClient.client.from(table)
            .select {
                filter {
                    eq("device_id", deviceId)
                    gte("date", startDate)
                    lte("date", endDate)
                }
            }
            .decodeList<Task>()
    }
}
