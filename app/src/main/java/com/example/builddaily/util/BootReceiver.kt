package com.example.builddaily.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.data.repository.TodoListRepository
import com.example.builddaily.util.today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                // 1. Reschedule Home Tasks for today
                val taskRepo = TaskRepository(context, "local_device") // Simplified deviceId
                val tasks = taskRepo.getTasksForDate(today().toString())
                tasks.forEach { TaskScheduler.scheduleTaskNotification(context, it) }

                // 2. Reschedule Todo Reminders
                val todoRepo = TodoListRepository(context)
                todoRepo.todos.value.filter { !it.isCompleted }.forEach { 
                    TodoScheduler.scheduleTodoReminder(context, it) 
                }

                // 3. Reschedule Hydration
                HydrationScheduler.reschedule(context)
                HydrationWatchdogWorker.schedule(context)
            }
        }
    }
}
