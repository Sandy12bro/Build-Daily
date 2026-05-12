package com.example.builddaily.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val _journals = MutableStateFlow(repository.getJournals())
    private val _stickyNotes = MutableStateFlow(repository.getStickyNotes())
    private val _monthlyCovers = MutableStateFlow(repository.getMonthlyCovers())
    private val _stats = MutableStateFlow(repository.getStats())
    
    // Auth locker states
    private val _isPasscodeConfigured = MutableStateFlow(repository.isAppLocked())
    private val _isSessionUnlocked = MutableStateFlow(!repository.isAppLocked())

    val isPasscodeConfigured: StateFlow<Boolean> = _isPasscodeConfigured.asStateFlow()
    val isSessionUnlocked: StateFlow<Boolean> = _isSessionUnlocked.asStateFlow()

    // Filters
    val searchQuery = MutableStateFlow("")
    val selectedFolder = MutableStateFlow("All Notes")
    val selectedMoodFilter = MutableStateFlow<String?>(null)
    val selectedDateFilter = MutableStateFlow<String?>(null)

    // AI Prompts helper pool
    val smartPrompts = listOf(
        "What made today meaningful?",
        "What distracted you today?",
        "What are you grateful for?",
        "What can improve tomorrow?",
        "Describe your dominant mood right now.",
        "What micro-habit helped you build daily?"
    )

    // Derived filtered journals state flow
    val filteredJournals = combine(
        _journals,
        searchQuery,
        selectedFolder,
        selectedMoodFilter,
        selectedDateFilter
    ) { list, query, folder, mood, date ->
        list.filter { entry ->
            val matchesQuery = query.isBlank() || 
                entry.title.contains(query, ignoreCase = true) || 
                entry.content.contains(query, ignoreCase = true)
            
            val matchesFolder = folder == "All Notes" || entry.folder.equals(folder, ignoreCase = true)
            val matchesMood = mood == null || entry.mood.equals(mood, ignoreCase = true)
            val matchesDate = date == null || entry.dateStr == date
            
            matchesQuery && matchesFolder && matchesMood && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, _journals.value)

    val stickyNotes: StateFlow<List<StickyNote>> = _stickyNotes.asStateFlow()
    val monthlyCovers: StateFlow<Map<String, MonthlyCoverConfig>> = _monthlyCovers.asStateFlow()
    val stats: StateFlow<JournalStats> = _stats.asStateFlow()

    fun refreshAll() {
        _journals.value = repository.getJournals()
        _stickyNotes.value = repository.getStickyNotes()
        _monthlyCovers.value = repository.getMonthlyCovers()
        _stats.value = repository.getStats()
        _isPasscodeConfigured.value = repository.isAppLocked()
    }

    // --- CRUD Journals ---
    fun addJournal(
        title: String,
        content: String,
        mood: String,
        moodIntensity: Float,
        textColorHex: String,
        tags: List<String>,
        folder: String,
        mediaType: String? = null
    ) {
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val entry = JournalEntry(
            title = title,
            content = content,
            dateStr = todayStr,
            mood = mood,
            moodIntensity = moodIntensity,
            textColorHex = textColorHex,
            tags = tags,
            folder = folder,
            mediaType = mediaType
        )
        repository.saveJournal(entry)
        refreshAll()
    }

    fun updateJournal(entry: JournalEntry) {
        repository.saveJournal(entry)
        refreshAll()
    }

    fun deleteJournal(id: String) {
        repository.deleteJournal(id)
        refreshAll()
    }

    fun togglePinJournal(entry: JournalEntry) {
        val updated = entry.copy(isPinned = !entry.isPinned)
        repository.saveJournal(updated)
        refreshAll()
    }

    // --- CRUD Sticky Notes ---
    fun addStickyNote(title: String, content: String, colorTheme: String, type: String) {
        val note = StickyNote(
            title = title,
            content = content,
            colorTheme = colorTheme,
            type = type
        )
        repository.saveStickyNote(note)
        refreshAll()
    }

    fun updateStickyNote(note: StickyNote) {
        repository.saveStickyNote(note)
        refreshAll()
    }

    fun deleteStickyNote(id: String) {
        repository.deleteStickyNote(id)
        refreshAll()
    }

    fun togglePinStickyNote(note: StickyNote) {
        val updated = note.copy(isPinned = !note.isPinned)
        repository.saveStickyNote(updated)
        refreshAll()
    }

    // --- Monthly Covers ---
    fun getOrCreateCoverConfigForMonth(monthYearStr: String): MonthlyCoverConfig {
        val map = _monthlyCovers.value
        return map[monthYearStr] ?: MonthlyCoverConfig(
            monthYearStr = monthYearStr,
            customTitle = generateDynamicTitleForMonth(monthYearStr)
        )
    }

    fun saveMonthlyCoverOverride(config: MonthlyCoverConfig) {
        repository.saveMonthlyCover(config)
        refreshAll()
    }

    private fun generateDynamicTitleForMonth(monthStr: String): String {
        return when {
            monthStr.startsWith("Jan") -> "The Reset Arc"
            monthStr.startsWith("Feb") -> "Discipline Engine"
            monthStr.startsWith("Mar") -> "Focus & Momentum"
            monthStr.startsWith("Apr") -> "Blossom Horizon"
            monthStr.startsWith("May") -> "The Renaissance"
            monthStr.startsWith("Jun") -> "Solar Clarity"
            monthStr.startsWith("Jul") -> "Zenith Endurance"
            monthStr.startsWith("Aug") -> "Velocity Surge"
            monthStr.startsWith("Sep") -> "Deep Work Frontier"
            monthStr.startsWith("Oct") -> "Autumn Reflections"
            monthStr.startsWith("Nov") -> "Absolute Consonance"
            else -> "Eternal Nexus Arc"
        }
    }

    // --- Security & Locker ---
    fun setupPasscode(pin: String) {
        repository.setPasscode(pin)
        _isPasscodeConfigured.value = true
        _isSessionUnlocked.value = true
    }

    fun disablePasscode() {
        repository.setPasscode("")
        _isPasscodeConfigured.value = false
        _isSessionUnlocked.value = true
    }

    fun verifyPasscode(inputPin: String): Boolean {
        val correct = repository.getPasscode() == inputPin
        if (correct) {
            _isSessionUnlocked.value = true
        }
        return correct
    }

    fun lockSessionManually() {
        if (repository.isAppLocked()) {
            _isSessionUnlocked.value = false
        }
    }
}
