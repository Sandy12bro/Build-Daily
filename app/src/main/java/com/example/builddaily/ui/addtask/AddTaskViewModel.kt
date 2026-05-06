package com.example.builddaily.ui.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Task
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.util.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddTaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val deviceId = repository.deviceId

    private var editingTaskId: String? = null
    private var initialTask: Task? = null

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val date = MutableStateFlow(today().toString())
    val startTime = MutableStateFlow("09:00")
    val endTime = MutableStateFlow("")
    val colorHex = MutableStateFlow<String?>(null)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // We could get all tasks for the date and find the one with this ID
                // For simplicity in demo mode, we'll assume the task is available or we can fetch it
                val tasks = repository.getTasksInRange(today().toString(), today().toString())
                val task = tasks.find { it.id == taskId }
                if (task != null) {
                    editingTaskId = taskId
                    initialTask = task
                    title.value = task.title
                    description.value = task.description ?: ""
                    date.value = task.date
                    startTime.value = task.startTime
                    endTime.value = task.endTime ?: ""
                    colorHex.value = task.colorHex
                }
            } catch (e: Exception) {
                _error.value = "Failed to load task details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onTitleChange(value: String) {
        title.value = value
    }

    fun onDescriptionChange(value: String) {
        description.value = value
    }

    fun onDateChange(value: String) {
        date.value = value
    }

    fun onStartTimeChange(value: String) {
        startTime.value = value
    }

    fun onEndTimeChange(value: String) {
        endTime.value = value
    }

    fun onColorChange(hex: String?) {
        colorHex.value = hex
    }

    fun saveTask() {
        if (title.value.isBlank()) {
            _error.value = "Title is required"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                if (editingTaskId != null && initialTask != null) {
                    val updatedTask = initialTask!!.copy(
                        title = title.value.trim(),
                        description = description.value.trim().ifBlank { null },
                        date = date.value,
                        startTime = startTime.value,
                        endTime = endTime.value.ifBlank { null },
                        colorHex = colorHex.value
                    )
                    repository.updateTask(updatedTask)
                } else {
                    val task = Task(
                        deviceId = deviceId,
                        title = title.value.trim(),
                        description = description.value.trim().ifBlank { null },
                        date = date.value,
                        startTime = startTime.value,
                        endTime = endTime.value.ifBlank { null },
                        colorHex = colorHex.value
                    )
                    repository.insertTask(task)
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save task"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
