package com.example.builddaily.ui.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch

class HydrationViewModel(
    private val repository: HydrationRepository,
    private val userStatsRepo: UserStatsRepository
) : ViewModel() {

    private val _config = MutableStateFlow(repository.getConfig())
    val config: StateFlow<HydrationGoalConfig> = _config.asStateFlow()

    private val _records = MutableStateFlow(repository.getRecords())
    val records: StateFlow<List<HydrationRecord>> = _records.asStateFlow()

    private val _stats = MutableStateFlow(repository.getStats())
    val stats: StateFlow<HydrationStats> = _stats.asStateFlow()

    private val _consumedTodayMl = MutableStateFlow(0)
    val consumedTodayMl: StateFlow<Int> = _consumedTodayMl.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val currentConfig = repository.getConfig()
        val currentRecords = repository.getRecords()
        val currentStats = repository.getStats()

        _config.value = currentConfig
        _records.value = currentRecords
        _stats.value = currentStats

        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val todaySum = currentRecords
            .filter { it.dateStr == todayStr && it.drinkType == "Water" }
            .sumOf { it.amountMl }
        
        _consumedTodayMl.value = todaySum
    }

    fun updateConfig(newConfig: HydrationGoalConfig) {
        repository.saveConfig(newConfig)
        refreshState()
    }

    fun addWaterIntake(amountMl: Int) {
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val record = HydrationRecord(
            amountMl = amountMl,
            dateStr = todayStr,
            drinkType = "Water"
        )
        
        val previouslyConsumed = _consumedTodayMl.value
        val goal = _config.value.calculatedGoalMl

        repository.saveRecord(record)
        
        // Award XP to user's central evolution tree!
        // Base 5 XP per water intake
        userStatsRepo.awardXP(5)

        // Check if this newly logged water crosses the threshold to hit the goal
        if (previouslyConsumed < goal && (previouslyConsumed + amountMl) >= goal) {
            // Milestone complete bonus!
            userStatsRepo.awardXP(25)
            // Trigger visual encouragement through action overlay
            com.example.builddaily.util.ActionMessageManager.postMessage(
                "Daily Hydration Goal Reached! +25 XP Bonus 💧✨",
                com.example.builddaily.util.ActionType.COMPLETED
            )
        } else {
            com.example.builddaily.util.ActionMessageManager.postMessage(
                "+$amountMl ml Hydration Tracked! +5 XP 💧",
                com.example.builddaily.util.ActionType.COMPLETED
            )
        }

        refreshState()
    }

    fun addCustomDrink(amountMl: Int, drinkType: String) {
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val record = HydrationRecord(
            amountMl = amountMl,
            dateStr = todayStr,
            drinkType = drinkType
        )
        repository.saveRecord(record)
        
        if (drinkType == "Caffeine") {
            com.example.builddaily.util.ActionMessageManager.postMessage(
                "Caffeine Logged! Remember to balance with water ☕",
                com.example.builddaily.util.ActionType.ADDED
            )
        } else {
            com.example.builddaily.util.ActionMessageManager.postMessage(
                "$drinkType Tracked!",
                com.example.builddaily.util.ActionType.ADDED
            )
        }

        refreshState()
    }

    fun deleteRecord(recordId: String) {
        repository.deleteRecord(recordId)
        refreshState()
        com.example.builddaily.util.ActionMessageManager.postMessage(
            "Record Removed",
            com.example.builddaily.util.ActionType.DELETED
        )
    }
}
