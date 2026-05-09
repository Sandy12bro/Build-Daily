package com.example.builddaily.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Task
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.util.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

enum class StatsPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class DayActivity(
    val completed: Int = 0,
    val total: Int = 0,
    val percentage: Float = 0f
)

data class StatsData(
    val labels: List<String> = emptyList(),
    val completedCounts: List<Int> = emptyList(),
    val totalCounts: List<Int> = emptyList(),
    val overallCompleted: Int = 0,
    val overallTotal: Int = 0,
    val streak: Int = 0,
    val dateRangeText: String = "",
    val heatmapData: Map<String, DayActivity> = emptyMap()
)

class StatsViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.DAILY)
    val period: StateFlow<StatsPeriod> = _period

    private val _referenceDate = MutableStateFlow(today())
    val referenceDate: StateFlow<LocalDate> = _referenceDate

    private val _statsData = MutableStateFlow(StatsData())
    val statsData: StateFlow<StatsData> = _statsData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadStats()
    }

    fun setPeriod(p: StatsPeriod) {
        _period.value = p
        _referenceDate.value = today() // Reset to today when switching periods
        loadStats()
    }

    fun setReferenceDate(date: LocalDate) {
        _referenceDate.value = date
        loadStats()
    }

    fun navigate(forward: Boolean) {
        val (value, unit) = when (_period.value) {
            StatsPeriod.DAILY -> 1 to DateTimeUnit.DAY
            StatsPeriod.WEEKLY -> 1 to DateTimeUnit.WEEK
            StatsPeriod.MONTHLY -> 1 to DateTimeUnit.MONTH
            StatsPeriod.YEARLY -> 1 to DateTimeUnit.YEAR
        }
        
        val nextDate = if (forward) {
            _referenceDate.value.plus(value, unit)
        } else {
            _referenceDate.value.minus(value, unit)
        }

        val todayDate = today()
        _referenceDate.value = if (forward && nextDate > todayDate) todayDate else nextDate
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val refDate = _referenceDate.value
                var startDate: LocalDate = refDate
                var endDate: LocalDate = refDate
                var labels: List<String> = emptyList()
                var dateRangeText: String = ""

                when (_period.value) {
                    StatsPeriod.DAILY -> {
                        startDate = refDate
                        endDate = refDate
                        labels = (0..23).map { h -> 
                            when {
                                h == 0 -> "12 AM"
                                h < 12 -> "$h AM"
                                h == 12 -> "12 PM"
                                else -> "${h - 12} PM"
                            }
                        }
                        dateRangeText = "${refDate.dayOfMonth} ${refDate.month.name.take(3)} ${refDate.year}"
                    }
                    StatsPeriod.WEEKLY -> {
                        startDate = refDate.minus(refDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
                        endDate = startDate.plus(6, DateTimeUnit.DAY)
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        dateRangeText = "${startDate.dayOfMonth} ${startDate.month.name.take(3)} - ${endDate.dayOfMonth} ${endDate.month.name.take(3)}"
                    }
                    StatsPeriod.MONTHLY -> {
                        startDate = LocalDate(refDate.year, refDate.monthNumber, 1)
                        endDate = startDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                        val daysCount = endDate.dayOfMonth
                        labels = (1..daysCount).map { it.toString() }
                        dateRangeText = "${refDate.month.name} ${refDate.year}"
                    }
                    StatsPeriod.YEARLY -> {
                        startDate = LocalDate(refDate.year, 1, 1)
                        endDate = LocalDate(refDate.year, 12, 31)
                        labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        dateRangeText = "${refDate.year}"
                    }
                }

                val allTasks = repository.getTasksInRange(startDate.toString(), endDate.toString())

                val (completed, total) = when (_period.value) {
                    StatsPeriod.DAILY -> {
                        val blocks = (0..23)
                        val comp = blocks.map { h -> 
                            allTasks.count { 
                                val hour = it.startTime.substringBefore(":").toIntOrNull() ?: 0
                                hour == h && it.isCompleted 
                            }
                        }
                        val tot = blocks.map { h -> 
                            allTasks.count { 
                                val hour = it.startTime.substringBefore(":").toIntOrNull() ?: 0
                                hour == h
                            }
                        }
                        comp to tot
                    }
                    StatsPeriod.WEEKLY -> {
                        val days = (0..6).map { startDate.plus(it, DateTimeUnit.DAY).toString() }
                        val comp = days.map { d -> allTasks.count { it.date == d && it.isCompleted } }
                        val tot = days.map { d -> allTasks.count { it.date == d } }
                        comp to tot
                    }
                    StatsPeriod.MONTHLY -> {
                        val daysCount = endDate.dayOfMonth
                        val days = (0 until daysCount).map { startDate.plus(it, DateTimeUnit.DAY).toString() }
                        val comp = days.map { d -> allTasks.count { it.date == d && it.isCompleted } }
                        val tot = days.map { d -> allTasks.count { it.date == d } }
                        comp to tot
                    }
                    StatsPeriod.YEARLY -> {
                        val months = (1..12)
                        val comp = months.map { m -> 
                            allTasks.count { 
                                val month = it.date.substring(5, 7).toIntOrNull() ?: 0
                                month == m && it.isCompleted 
                            } 
                        }
                        val tot = months.map { m -> 
                            allTasks.count { 
                                val month = it.date.substring(5, 7).toIntOrNull() ?: 0
                                month == m
                            }
                        }
                        comp to tot
                    }
                }

                val streak = calculateStreak(repository.getTasksInRange(startDate.minus(30, DateTimeUnit.DAY).toString(), endDate.toString()), endDate)

                // Heatmap logic: Get tasks for last 365 days
                val heatmapStartDate = today().minus(364, DateTimeUnit.DAY)
                val yearTasks = repository.getTasksInRange(heatmapStartDate.toString(), today().toString())
                val heatmapMap = yearTasks.groupBy { it.date }.mapValues { (_, tasks) ->
                    val comp = tasks.count { it.isCompleted }
                    val tot = tasks.size
                    DayActivity(
                        completed = comp,
                        total = tot,
                        percentage = if (tot > 0) comp.toFloat() / tot else 0f
                    )
                }

                _statsData.value = StatsData(
                    labels = labels,
                    completedCounts = completed,
                    totalCounts = total,
                    overallCompleted = allTasks.count { it.isCompleted },
                    overallTotal = allTasks.size,
                    streak = streak,
                    dateRangeText = dateRangeText,
                    heatmapData = heatmapMap
                )
            } catch (_: Exception) {
                _statsData.value = StatsData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateStreak(tasks: List<Task>, fromDate: LocalDate): Int {
        if (tasks.isEmpty()) return 0
        val minDate = tasks.minOf { it.date }
        var streak = 0
        var date = fromDate
        while (date.toString() >= minDate) {
            val dayTasks = tasks.filter { it.date == date.toString() }
            if (dayTasks.isEmpty()) {
                // Check if we are still within the same year/range or just stop if no data
                // For streak, we usually stop if there's a gap in days with tasks.
                // But if there are no tasks on a day, we might not want to break the streak?
                // Standard: if no tasks scheduled, streak continues.
                date = date.minus(1, DateTimeUnit.DAY)
                continue 
            }
            val completed = dayTasks.count { it.isCompleted }
            val completionRate = completed.toFloat() / dayTasks.size
            
            if (completionRate >= 0.75f) { // 75% threshold
                streak++
                date = date.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
            
            // Limit to prevent infinite loop just in case
            if (streak > 365) break
        }
        return streak
    }
}
