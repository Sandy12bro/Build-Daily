package com.example.builddaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.di.RepositoryModule
import com.example.builddaily.data.models.Day
import com.example.builddaily.data.models.Task
import com.example.builddaily.data.models.TaskStatus
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayViewModel : ViewModel() {
    
    private val dayRepository = RepositoryModule.provideDayRepository()
    private val taskRepository = RepositoryModule.provideTaskRepository()
    
    private val _todayDay = MutableStateFlow<Day?>(null)
    val todayDay: StateFlow<Day?> = _todayDay.asStateFlow()
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val currentUserId = "current-user" // TODO: Get from auth
    
    init {
        checkOrCreateToday()
    }
    
    private fun checkOrCreateToday() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val today = LocalDate.now()
            val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            when (val result = dayRepository.getDayByDate(dateString, currentUserId)) {
                is NetworkResult.Success -> {
                    if (result.data != null) {
                        _todayDay.value = result.data
                        loadTasks(result.data.id!!)
                    } else {
                        createNewDay(dateString)
                    }
                }
                is NetworkResult.Error -> {
                    // Handle error - could show snackbar or log
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }
    
    private suspend fun createNewDay(dateString: String) {
        val newDay = Day(
            userId = currentUserId,
            date = dateString,
            completionRate = 0.0f
        )
        
        when (val result = dayRepository.createDay(newDay)) {
            is NetworkResult.Success -> {
                _todayDay.value = result.data
                loadTasks(result.data.id!!)
            }
            is NetworkResult.Error -> {
                // Handle error
            }
            is NetworkResult.Loading -> {
                // Already loading
            }
        }
        
        _isLoading.value = false
    }
    
    private fun loadTasks(dayId: String) {
        viewModelScope.launch {
            when (val result = taskRepository.getTasksByDay(dayId, currentUserId)) {
                is NetworkResult.Success -> {
                    _tasks.value = result.data
                }
                is NetworkResult.Error -> {
                    // Handle error
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }
    
    fun addTask(task: Task) {
        viewModelScope.launch {
            when (val result = taskRepository.createTask(task)) {
                is NetworkResult.Success -> {
                    val updatedTasks = _tasks.value + result.data
                    _tasks.value = updatedTasks
                    updateCompletionRate()
                }
                is NetworkResult.Error -> {
                    // Handle error
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }
    
    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = if (task.status == TaskStatus.DONE) TaskStatus.PENDING else TaskStatus.DONE
            )
            
            when (val result = taskRepository.updateTask(updatedTask)) {
                is NetworkResult.Success -> {
                    val updatedTasks = _tasks.value.map { if (it.id == task.id) result.data else it }
                    _tasks.value = updatedTasks
                    updateCompletionRate()
                }
                is NetworkResult.Error -> {
                    // Handle error
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }
    
    private fun updateCompletionRate() {
        val todayDay = _todayDay.value ?: return
        val completedTasks = _tasks.value.count { it.status == TaskStatus.DONE }
        val totalTasks = _tasks.value.size
        
        if (totalTasks > 0) {
            val newRate = completedTasks.toFloat() / totalTasks.toFloat()
            val updatedDay = todayDay.copy(completionRate = newRate)
            
            viewModelScope.launch {
                when (val result = dayRepository.updateDay(updatedDay)) {
                    is NetworkResult.Success -> {
                        _todayDay.value = result.data
                    }
                    is NetworkResult.Error -> {
                        // Handle error
                    }
                    is NetworkResult.Loading -> {
                        // Already loading
                    }
                }
            }
        }
    }
    
    fun getDisplayDate(): String {
        val today = LocalDate.now()
        return today.format(DateTimeFormatter.ofPattern("MMMM d"))
    }
}
