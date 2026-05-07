package com.example.builddaily.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Task
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.util.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class HistoryViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _tasks.value = repository.getTasksForDate(_selectedDate.value.toString())
            } catch (_: Exception) {
                _tasks.value = emptyList()
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
            } catch (_: Exception) {}
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task.id)
                _tasks.value = _tasks.value.filter { it.id != task.id }
            } catch (_: Exception) {}
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
            } catch (_: Exception) {}
        }
    }
}
