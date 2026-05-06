package com.example.builddaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.di.RepositoryModule
import com.example.builddaily.data.models.Day
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel : ViewModel() {
    
    private val dayRepository = RepositoryModule.provideDayRepository()
    
    private val _days = MutableStateFlow<List<Day>>(emptyList())
    val days: StateFlow<List<Day>> = _days.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val currentUserId = "current-user" // TODO: Get from auth
    
    init {
        loadAllDays()
    }

    private fun loadAllDays() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = dayRepository.getAllDays(currentUserId)) {
                is NetworkResult.Success -> {
                    _days.value = result.data ?: emptyList()
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
                is NetworkResult.Error -> {
                    _isLoading.value = false
                }
            }
        }
    }
}
