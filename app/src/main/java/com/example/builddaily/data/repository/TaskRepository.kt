package com.example.builddaily.data.repository

import com.example.builddaily.data.SupabaseClient
import com.example.builddaily.data.model.Task
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class TaskRepository(private val deviceId: String) {

    private val table = "tasks"

    suspend fun getTasksForDate(date: String): List<Task> {
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
        return SupabaseClient.client.from(table)
            .insert(task) { select() }
            .decodeSingle<Task>()
    }

    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        SupabaseClient.client.from(table)
            .update({ set("is_completed", isCompleted) }) {
                filter { eq("id", taskId) }
            }
    }

    suspend fun deleteTask(taskId: String) {
        SupabaseClient.client.from(table)
            .delete {
                filter { eq("id", taskId) }
            }
    }

    suspend fun getTasksInRange(startDate: String, endDate: String): List<Task> {
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
