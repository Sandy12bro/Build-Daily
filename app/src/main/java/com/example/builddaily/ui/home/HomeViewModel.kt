package com.example.builddaily.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Task
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import com.example.builddaily.util.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _tasks.value = repository.getTasksForDate(today().toString())
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load tasks"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            try {
                val newStatus = !task.isCompleted
                repository.updateTaskCompletion(task.id, newStatus)
                _tasks.value = _tasks.value.map {
                    if (it.id == task.id) it.copy(isCompleted = newStatus) else it
                }
                ActionMessageManager.emit(
                    if (newStatus) "Task completed! 🤩" else "Task set to incomplete",
                    if (newStatus) ActionType.COMPLETED else ActionType.INCOMPLETE
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task.id)
                _tasks.value = _tasks.value.filter { it.id != task.id }
                ActionMessageManager.emit("Task deleted 🗑️", ActionType.DELETED)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun repeatTask(task: Task) {
        viewModelScope.launch {
            try {
                val newTask = task.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    date = today().toString(),
                    isCompleted = false,
                    createdAt = kotlinx.datetime.Clock.System.now().toString()
                )
                repository.insertTask(newTask)
                ActionMessageManager.emit("Task repeated for today 🔄", ActionType.REPEATED)
                loadTasks() // Reload to show the new duplicated task
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
