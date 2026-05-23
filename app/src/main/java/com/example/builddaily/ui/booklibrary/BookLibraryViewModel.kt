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

    val todayPages = readingLogs.map { logs ->
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        logs.filter { it.dateStr == todayStr }.sumOf { it.pagesRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekPages = readingLogs.map { logs ->
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        logs.filter { 
            try {
                val recordDate = LocalDate.parse(it.dateStr)
                recordDate.daysUntil(today) in 0..6
            } catch(e: Exception) { false }
        }.sumOf { it.pagesRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthPages = readingLogs.map { logs ->
        val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val currentMonth = todayStr.substring(0, 7) // YYYY-MM
        logs.filter { it.dateStr.startsWith(currentMonth) }.sumOf { it.pagesRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


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
