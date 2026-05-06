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
    private val repository: TaskRepository,
    private val deviceId: String
) : ViewModel() {

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val date = MutableStateFlow(today().toString())
    val startTime = MutableStateFlow("09:00")
    val endTime = MutableStateFlow("")

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun saveTask() {
        if (title.value.isBlank()) {
            _error.value = "Title is required"
            return
        }
        if (startTime.value.isBlank()) {
            _error.value = "Start time is required"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                val task = Task(
                    deviceId = deviceId,
                    title = title.value.trim(),
                    description = description.value.trim().ifBlank { null },
                    date = date.value,
                    startTime = startTime.value,
                    endTime = endTime.value.ifBlank { null }
                )
                repository.insertTask(task)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save task"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
