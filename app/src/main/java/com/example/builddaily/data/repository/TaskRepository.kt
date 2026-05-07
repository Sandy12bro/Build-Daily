package com.example.builddaily.data.repository

import com.example.builddaily.data.SupabaseClient
import com.example.builddaily.data.model.Task
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

import android.content.Context
import com.example.builddaily.util.TaskScheduler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TaskRepository(private val context: Context, val deviceId: String) {

    private val table = "tasks"
    private val isDemoMode = SupabaseClient.client.supabaseUrl.contains("your-project.supabase.co") || SupabaseClient.client.supabaseUrl.isBlank()
    private val prefs = context.getSharedPreferences("build_daily_demo_tasks", Context.MODE_PRIVATE)

    companion object {
        private val mockTasks = mutableListOf<Task>()
        private var isInitialized = false
        private val jsonConfig = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    init {
        if (isDemoMode && !isInitialized) {
            val savedTasksJson = prefs.getString("mock_tasks_json", "[]") ?: "[]"
            try {
                val savedTasks = jsonConfig.decodeFromString<List<Task>>(savedTasksJson)
                mockTasks.clear()
                mockTasks.addAll(savedTasks)
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveDemoTasks() {
        if (isDemoMode) {
            val json = jsonConfig.encodeToString(mockTasks.toList())
            prefs.edit().putString("mock_tasks_json", json).commit()
        }
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
        val insertedTask = if (isDemoMode) {
            val newTask = task.copy(id = java.util.UUID.randomUUID().toString())
            mockTasks.add(newTask)
            saveDemoTasks()
            newTask
        } else {
            SupabaseClient.client.from(table)
                .insert(task) { select() }
                .decodeSingle<Task>()
        }
        TaskScheduler.scheduleTaskNotification(context, insertedTask)
        return insertedTask
    }

    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        if (isDemoMode) {
            val index = mockTasks.indexOfFirst { it.id == taskId }
            if (index != -1) {
                mockTasks[index] = mockTasks[index].copy(isCompleted = isCompleted)
                saveDemoTasks()
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
            saveDemoTasks()
        } else {
            SupabaseClient.client.from(table)
                .delete {
                    filter { eq("id", taskId) }
                }
        }
        TaskScheduler.cancelTaskNotification(context, Task(id = taskId))
    }

    suspend fun updateTask(task: Task) {
        if (isDemoMode) {
            val index = mockTasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                mockTasks[index] = task
                saveDemoTasks()
            }
        } else {
            SupabaseClient.client.from(table)
                .update(task) {
                    filter { eq("id", task.id) }
                }
        }
        TaskScheduler.scheduleTaskNotification(context, task)
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
