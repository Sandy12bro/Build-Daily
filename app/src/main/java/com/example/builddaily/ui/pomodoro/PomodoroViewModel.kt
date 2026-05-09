package com.example.builddaily.ui.pomodoro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.PomodoroSession
import com.example.builddaily.data.model.PomodoroStats
import com.example.builddaily.data.repository.PomodoroRepository
import com.example.builddaily.service.PomodoroService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PomodoroViewModel(private val repository: PomodoroRepository) : ViewModel() {
    private val _sessions = MutableStateFlow<List<PomodoroSession>>(emptyList())
    val sessions: StateFlow<List<PomodoroSession>> = _sessions

    private val _stats = MutableStateFlow(PomodoroStats())
    val stats: StateFlow<PomodoroStats> = _stats

    private val _mode = MutableStateFlow(PomodoroMode.POMODORO)
    val mode: StateFlow<PomodoroMode> = _mode

    private val _focusDuration = MutableStateFlow(25)
    val focusDuration: StateFlow<Int> = _focusDuration

    private val _shortBreakDuration = MutableStateFlow(5)
    val shortBreakDuration: StateFlow<Int> = _shortBreakDuration

    private val _longBreakDuration = MutableStateFlow(15)
    val longBreakDuration: StateFlow<Int> = _longBreakDuration

    private val _timeLeft = MutableStateFlow(25 * 60)
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    init {
        loadData()
        
        // Synchronize with Service state
        viewModelScope.launch {
            PomodoroService.timeLeft.collectLatest { _timeLeft.value = it }
        }
        viewModelScope.launch {
            PomodoroService.isRunning.collectLatest { _isRunning.value = it }
        }
        viewModelScope.launch {
            PomodoroService.isCompleted.collectLatest { 
                _isCompleted.value = it
                if (it) loadData() // Refresh stats on completion
            }
        }
    }

    fun loadData() {
        _sessions.value = repository.getSessions()
        _stats.value = repository.getStats()
    }

    fun dismissCompletion() {
        PomodoroService.dismissCompletion()
        loadData()
    }

    fun setMode(newMode: PomodoroMode) {
        _mode.value = newMode
        resetTimer()
    }

    fun setDurations(focus: Int, short: Int, long: Int) {
        _focusDuration.value = focus
        _shortBreakDuration.value = short
        _longBreakDuration.value = long
        resetTimer()
    }

    fun toggleTimer(context: Context) {
        if (_isRunning.value) {
            pauseTimer(context)
        } else {
            startTimer(context)
        }
    }

    private fun startTimer(context: Context) {
        val duration = if (_timeLeft.value <= 0) {
            val d = when (_mode.value) {
                PomodoroMode.POMODORO -> _focusDuration.value
                PomodoroMode.SHORT_BREAK -> _shortBreakDuration.value
                PomodoroMode.LONG_BREAK -> _longBreakDuration.value
            }
            d * 60
        } else {
            _timeLeft.value
        }
        PomodoroService.start(context, duration, _mode.value.label)
    }

    fun pauseTimer(context: Context) {
        PomodoroService.stop(context)
        loadData()
    }

    fun resetTimer() {
        val duration = when (_mode.value) {
            PomodoroMode.POMODORO -> _focusDuration.value
            PomodoroMode.SHORT_BREAK -> _shortBreakDuration.value
            PomodoroMode.LONG_BREAK -> _longBreakDuration.value
        }
        _timeLeft.value = duration * 60
        PomodoroService.timeLeft.value = duration * 60
        PomodoroService.isRunning.value = false
    }
}
