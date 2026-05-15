package com.example.builddaily.ui.booklibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.Book
import com.example.builddaily.data.model.BookStatus
import com.example.builddaily.data.repository.BookRepository
import com.example.builddaily.data.repository.ReadingGoal
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookLibraryViewModel(private val repository: BookRepository) : ViewModel() {

    val books = repository.books
    val readingGoal = repository.readingGoal

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    // Optimized derived states
    val currentlyReading = books.map { list ->
        list.filter { it.status == BookStatus.CURRENTLY_READING }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wantToRead = books.map { list ->
        list.filter { it.status == BookStatus.WANT_TO_READ }.sortedByDescending { it.priority.level }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completed = books.map { list ->
        list.filter { it.status == BookStatus.COMPLETED }.sortedByDescending { it.completedDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites = books.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archived = books.map { list ->
        list.filter { it.status == BookStatus.ARCHIVED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic stats to prevent ghost counts
    val totalPagesRead = books.map { it.sumOf { book -> book.pagesRead } }
    val completedCount = completed.map { it.size }
    val totalBooks = books.map { it.size }

    val displayedBooks = combine(
        currentlyReading, wantToRead, completed, favorites, _selectedTab
    ) { reading, want, done, favs, tab ->
        when (tab) {
            0 -> reading
            1 -> want
            2 -> done
            3 -> favs
            else -> reading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            repository.addBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            // Enforce pagesRead limit
            val validatedBook = book.copy(
                pagesRead = book.pagesRead.coerceIn(0, if (book.pages > 0) book.pages else Int.MAX_VALUE)
            )
            repository.updateBook(validatedBook)
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            repository.deleteBook(id)
        }
    }

    fun updateReadingGoal(goal: ReadingGoal) {
        viewModelScope.launch {
            repository.updateReadingGoal(goal)
        }
    }

    fun updateProgress(book: Book, newPages: Int) {
        viewModelScope.launch {
            val pages = newPages.coerceIn(0, book.pages)
            val isNewlyCompleted = pages >= book.pages && book.pages > 0 && book.status != BookStatus.COMPLETED
            
            val updatedBook = book.copy(
                pagesRead = pages,
                status = if (isNewlyCompleted) BookStatus.COMPLETED else book.status,
                completedDate = if (isNewlyCompleted) {
                    kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
                } else book.completedDate
            )
            repository.updateBook(updatedBook)
        }
    }
}
