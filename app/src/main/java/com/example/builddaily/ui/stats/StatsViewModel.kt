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

enum class StatsPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

data class StatsData(
    val labels: List<String> = emptyList(),
    val completedCounts: List<Int> = emptyList(),
    val totalCounts: List<Int> = emptyList(),
    val overallCompleted: Int = 0,
    val overallTotal: Int = 0,
    val streak: Int = 0
)

class StatsViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.DAILY)
    val period: StateFlow<StatsPeriod> = _period

    private val _statsData = MutableStateFlow(StatsData())
    val statsData: StateFlow<StatsData> = _statsData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadStats()
    }

    fun setPeriod(p: StatsPeriod) {
        _period.value = p
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val todayDate = today()
                val (startDate, labels) = when (_period.value) {
                    StatsPeriod.DAILY -> {
                        val start = todayDate.minus(6, DateTimeUnit.DAY)
                        val lbls = (0..6).map { todayDate.minus(6 - it, DateTimeUnit.DAY) }
                        start to lbls.map { "${it.dayOfMonth}/${it.monthNumber}" }
                    }
                    StatsPeriod.WEEKLY -> {
                        val start = todayDate.minus(27, DateTimeUnit.DAY)
                        start to listOf("W4", "W3", "W2", "W1")
                    }
                    StatsPeriod.MONTHLY -> {
                        val start = todayDate.minus(11, DateTimeUnit.MONTH)
                        val lbls = (0..11).map {
                            val d = todayDate.minus(11 - it, DateTimeUnit.MONTH)
                            "${d.monthNumber}/${d.year % 100}"
                        }
                        start to lbls
                    }
                    StatsPeriod.YEARLY -> {
                        val start = todayDate.minus(2, DateTimeUnit.YEAR)
                        val lbls = (0..2).map { (todayDate.year - 2 + it).toString() }
                        start to lbls
                    }
                }
                val allTasks = repository.getTasksInRange(startDate.toString(), todayDate.toString())

                val (completed, total) = when (_period.value) {
                    StatsPeriod.DAILY -> {
                        val days = (0..6).map { todayDate.minus(6 - it, DateTimeUnit.DAY).toString() }
                        val comp = days.map { d -> allTasks.count { it.date == d && it.isCompleted } }
                        val tot = days.map { d -> allTasks.count { it.date == d } }
                        comp to tot
                    }
                    StatsPeriod.WEEKLY -> {
                        val weeks = (0..3).map { w ->
                            val weekStart = todayDate.minus((3 - w) * 7 + 6, DateTimeUnit.DAY)
                            val weekEnd = todayDate.minus((3 - w) * 7, DateTimeUnit.DAY)
                            weekStart.toString() to weekEnd.toString()
                        }
                        val comp = weeks.map { (s, e) -> allTasks.count { it.date >= s && it.date <= e && it.isCompleted } }
                        val tot = weeks.map { (s, e) -> allTasks.count { it.date >= s && it.date <= e } }
                        comp to tot
                    }
                    StatsPeriod.MONTHLY -> {
                        val months = (0..11).map { todayDate.minus(11 - it, DateTimeUnit.MONTH) }
                        val comp = months.map { m -> allTasks.count { LocalDate.parse(it.date).monthNumber == m.monthNumber && LocalDate.parse(it.date).year == m.year && it.isCompleted } }
                        val tot = months.map { m -> allTasks.count { LocalDate.parse(it.date).monthNumber == m.monthNumber && LocalDate.parse(it.date).year == m.year } }
                        comp to tot
                    }
                    StatsPeriod.YEARLY -> {
                        val years = (0..2).map { todayDate.year - 2 + it }
                        val comp = years.map { y -> allTasks.count { LocalDate.parse(it.date).year == y && it.isCompleted } }
                        val tot = years.map { y -> allTasks.count { LocalDate.parse(it.date).year == y } }
                        comp to tot
                    }
                }

                val streak = calculateStreak(allTasks, todayDate)

                _statsData.value = StatsData(
                    labels = labels,
                    completedCounts = completed,
                    totalCounts = total,
                    overallCompleted = allTasks.count { it.isCompleted },
                    overallTotal = allTasks.size,
                    streak = streak
                )
            } catch (_: Exception) {
                _statsData.value = StatsData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateStreak(tasks: List<Task>, fromDate: LocalDate): Int {
        var streak = 0
        var date = fromDate
        while (true) {
            val dayTasks = tasks.filter { it.date == date.toString() }
            if (dayTasks.isEmpty()) break
            if (dayTasks.all { it.isCompleted }) {
                streak++
                date = date.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        return streak
    }
}
