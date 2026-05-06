package com.example.builddaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.di.RepositoryModule
import com.example.builddaily.data.models.Day
import com.example.builddaily.data.models.Diary
import com.example.builddaily.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryViewModel : ViewModel() {
    
    private val dayRepository = RepositoryModule.provideDayRepository()
    private val diaryRepository = RepositoryModule.provideDiaryRepository()
    
    private val _diary = MutableStateFlow<Diary?>(null)
    val diary: StateFlow<Diary?> = _diary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayDay = MutableStateFlow<Day?>(null)

    private val currentUserId = "current-user" // TODO: Get from auth
    
    init {
        loadTodayAndDiary()
    }

    private fun loadTodayAndDiary() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val today = LocalDate.now()
            val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            when (val dayResult = dayRepository.getDayByDate(dateString, currentUserId)) {
                is NetworkResult.Success -> {
                    _todayDay.value = dayResult.data
                    dayResult.data?.id?.let { dayId ->
                        when (val diaryResult = diaryRepository.getDiaryByDay(dayId)) {
                            is NetworkResult.Success -> {
                                _diary.value = diaryResult.data
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

    fun saveDiary(text: String, mood: com.example.builddaily.data.models.Mood) {
        val today = _todayDay.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            
            val existingDiary = _diary.value
            if (existingDiary != null) {
                val updated = existingDiary.copy(text = text, mood = mood)
                when (val result = diaryRepository.updateDiary(updated)) {
                    is NetworkResult.Success -> _diary.value = result.data
                    else -> {}
                }
            } else {
                val newDiary = Diary(
                    userId = currentUserId,
                    dayId = today.id!!,
                    text = text,
                    mood = mood
                )
                when (val result = diaryRepository.createDiary(newDiary)) {
                    is NetworkResult.Success -> _diary.value = result.data
                    else -> {}
                }
            }
            _isLoading.value = false
        }
    }
}
