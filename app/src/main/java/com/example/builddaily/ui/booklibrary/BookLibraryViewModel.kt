package com.example.builddaily.ui.booklibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Book
import com.example.builddaily.data.model.ReadingStatus
import com.example.builddaily.data.repository.BookRepository
import com.example.builddaily.data.repository.ReadingGoal
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

enum class SortOption(val displayName: String) {
    TITLE("Title"),
    AUTHOR("Author"),
    PRIORITY("Priority"),
    PAGES("Number of Pages"),
    PRICE("Price"),
    RECENT_ADDED("Recently Added"),
    LANGUAGE("Language"),
    CATEGORY("Category"),
    PROGRESS("Reading Progress")
}

enum class ReadingViewMode { DAY, WEEK, MONTH, YEAR }

class BookLibraryViewModel(private val repository: BookRepository) : ViewModel() {

    val books = repository.books
    val readingGoal = repository.readingGoal

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECENT_ADDED)
    val sortOption = _sortOption.asStateFlow()

    // Optimized derived states
    val currentlyReading = combine(books, _sortOption) { list, sort ->
        sortBooks(list.filter { it.status == ReadingStatus.READING }, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toRead = combine(books, _sortOption) { list, sort ->
        sortBooks(list.filter { it.status == ReadingStatus.WANT }, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completed = combine(books, _sortOption) { list, sort ->
        sortBooks(list.filter { it.status == ReadingStatus.DONE }, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteStatusBooks = combine(books, _sortOption) { list, sort ->
        sortBooks(list.filter { it.isFavorite }, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Analytics
    val totalPagesRead = books.map { list -> list.sumOf { it.pagesRead } }
    val completedCount = completed.map { list -> list.size }

    val readingLogs = repository.readingLogs

    private val _selectedDate = MutableStateFlow(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    val selectedDate = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(ReadingViewMode.DAY)
    val viewMode = _viewMode.asStateFlow()

    val aggregatedPages = combine(readingLogs, _selectedDate, _viewMode) { logs, date, mode ->
        logs.filter { record ->
            try {
                val recordDate = LocalDate.parse(record.dateStr)
                when (mode) {
                    ReadingViewMode.DAY -> recordDate == date
                    ReadingViewMode.WEEK -> {
                        // 7-day window ending on the selectedDate
                        val diff = recordDate.daysUntil(date)
                        diff in 0..6
                    }
                    ReadingViewMode.MONTH -> recordDate.year == date.year && recordDate.month == date.month
                    ReadingViewMode.YEAR -> recordDate.year == date.year
                }
            } catch(e: Exception) { false }
        }.sumOf { it.pagesRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setViewMode(mode: ReadingViewMode) {
        _viewMode.value = mode
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    private fun sortBooks(list: List<Book>, option: SortOption): List<Book> {
        return when (option) {
            SortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            SortOption.AUTHOR -> list.sortedBy { it.author.lowercase() }
            SortOption.PRIORITY -> list.sortedByDescending { it.priority.level }
            SortOption.PAGES -> list.sortedByDescending { it.totalPages }
            SortOption.PRICE -> list.sortedByDescending { it.price }
            SortOption.RECENT_ADDED -> list.sortedByDescending { it.createdAt }
            SortOption.LANGUAGE -> list.sortedBy { it.language.lowercase() }
            SortOption.CATEGORY -> list.sortedBy { it.genre.lowercase() }
            SortOption.PROGRESS -> list.sortedByDescending { it.progress }
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            repository.addBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            repository.deleteBook(id)
        }
    }

    fun adjustPages(bookId: String, delta: Int) {
        viewModelScope.launch {
            repository.adjustPages(bookId, delta)
        }
    }

    fun setPagesRead(bookId: String, pages: Int) {
        viewModelScope.launch {
            repository.setPagesRead(bookId, pages)
        }
    }

    fun updateReadingGoal(goal: ReadingGoal) {
        viewModelScope.launch {
            repository.updateReadingGoal(goal)
        }
    }
}
