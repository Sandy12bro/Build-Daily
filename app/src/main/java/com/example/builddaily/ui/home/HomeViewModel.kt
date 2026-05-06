package com.example.builddaily.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Task
import com.example.builddaily.data.repository.TaskRepository
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
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
