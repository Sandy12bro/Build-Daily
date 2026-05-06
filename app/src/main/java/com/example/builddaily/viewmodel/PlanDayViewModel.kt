package com.example.builddaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.di.RepositoryModule
import com.example.builddaily.data.models.Day
import com.example.builddaily.data.models.EnergyType
import com.example.builddaily.data.models.Task
import com.example.builddaily.data.models.TaskStatus
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class PlanDayViewModel : ViewModel() {
    
    private val dayRepository = RepositoryModule.provideDayRepository()
    private val taskRepository = RepositoryModule.provideTaskRepository()
    
    private val _yesterdaysTasks = MutableStateFlow<List<Task>>(emptyList())
    val yesterdaysTasks: StateFlow<List<Task>> = _yesterdaysTasks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayDay = MutableStateFlow<Day?>(null)

    private val currentUserId = "current-user" // TODO: Get from auth
    
    init {
        loadTodayAndYesterday()
    }

    private fun loadTodayAndYesterday() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            
            val todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val yesterdayString = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE)

            // Load today to attach new tasks to it
            when (val todayResult = dayRepository.getDayByDate(todayString, currentUserId)) {
                is NetworkResult.Success -> {
                    _todayDay.value = todayResult.data
                }
                else -> {}
            }

            // Load yesterday's tasks
            when (val yesterdayResult = dayRepository.getDayByDate(yesterdayString, currentUserId)) {
                is NetworkResult.Success -> {
                    yesterdayResult.data?.id?.let { yesterdayId ->
                        when (val tasksResult = taskRepository.getTasksByDay(yesterdayId, currentUserId)) {
                            is NetworkResult.Success -> {
                                _yesterdaysTasks.value = tasksResult.data
                            }
                            else -> {}
                        }
                    }
                }
                else -> {}
            }
            
            _isLoading.value = false
        }
    }

    fun addTask(title: String, energyType: EnergyType, time: String) {
        val today = _todayDay.value ?: return
        if (title.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val newTask = Task(
                dayId = today.id!!,
                title = title.trim(),
                energyType = energyType,
                time = time,
                status = TaskStatus.PENDING,
                userId = currentUserId
            )
            taskRepository.createTask(newTask)
            _isLoading.value = false
        }
    }

    fun repeatTasks(tasksToRepeat: List<Task>) {
        val today = _todayDay.value ?: return
        if (tasksToRepeat.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            tasksToRepeat.forEach { oldTask ->
                val newTask = Task(
                    dayId = today.id!!,
                    title = oldTask.title,
                    energyType = oldTask.energyType,
                    time = oldTask.time,
                    status = TaskStatus.PENDING,
                    userId = currentUserId
                )
                taskRepository.createTask(newTask)
            }
            _isLoading.value = false
        }
    }
}
