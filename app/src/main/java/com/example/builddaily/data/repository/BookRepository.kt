package com.example.builddaily.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.builddaily.data.model.Book
import com.example.builddaily.data.model.BookPriority
import com.example.builddaily.data.model.ReadingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

class BookRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("book_library_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _books = MutableStateFlow<List<Book>>(loadBooks())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _readingGoal = MutableStateFlow(loadReadingGoal())
    val readingGoal: StateFlow<ReadingGoal> = _readingGoal.asStateFlow()

    private fun loadBooks(): List<Book> {
        val jsonStr = prefs.getString("books", "[]") ?: "[]"
        return try { json.decodeFromString<List<Book>>(jsonStr) } catch (e: Exception) { emptyList() }
    }

    private fun saveBooks(books: List<Book>) {
        prefs.edit().putString("books", json.encodeToString(books)).apply()
        _books.value = books
    }

    private fun loadReadingGoal(): ReadingGoal {
        return ReadingGoal(
            yearlyGoal = prefs.getInt("yearly_goal", 12),
            monthlyGoal = prefs.getInt("monthly_goal", 1),
            pagesPerDay = prefs.getInt("pages_per_day", 20),
            booksReadThisYear = prefs.getInt("books_this_year", 0),
            currentStreak = prefs.getInt("reading_streak", 0)
        )
    }

    private fun saveReadingGoal(goal: ReadingGoal) {
        prefs.edit()
            .putInt("yearly_goal", goal.yearlyGoal)
            .putInt("monthly_goal", goal.monthlyGoal)
            .putInt("pages_per_day", goal.pagesPerDay)
            .putInt("books_this_year", goal.booksReadThisYear)
            .putInt("reading_streak", goal.currentStreak)
            .apply()
        _readingGoal.value = goal
    }

    fun addBook(book: Book) {
        val updated = _books.value + book
        saveBooks(updated)
    }

    fun updateBook(book: Book) {
        val updated = _books.value.map { if (it.id == book.id) book else it }
        saveBooks(updated)
    }

    fun deleteBook(id: String) {
        val updated = _books.value.filter { it.id != id }
        saveBooks(updated)
    }

    fun adjustPages(id: String, delta: Int) {
        val updated = _books.value.map {
            if (it.id == id) {
                val newPagesRead = (it.pagesRead + delta).coerceIn(0, it.totalPages)
                updateBookProgress(it, newPagesRead)
            } else it
        }
        saveBooks(updated)
    }

    fun setPagesRead(id: String, pages: Int) {
        val updated = _books.value.map {
            if (it.id == id) {
                val newPagesRead = pages.coerceIn(0, it.totalPages)
                updateBookProgress(it, newPagesRead)
            } else it
        }
        saveBooks(updated)
    }

    fun updateReadingGoal(goal: ReadingGoal) {
        saveReadingGoal(goal)
    }

    private fun updateBookProgress(book: Book, newPagesRead: Int): Book {
        val isNowCompleted = newPagesRead >= book.totalPages && book.totalPages > 0
        val newStatus = if (isNowCompleted) ReadingStatus.DONE else book.status
        val completedDate = if (isNowCompleted && book.completedDate == null) {
            Clock.System.now().toString().substringBefore("T")
        } else if (!isNowCompleted) null else book.completedDate
        
        if (isNowCompleted && book.status != ReadingStatus.DONE) {
            incrementBooksRead()
        }
        
        return book.copy(
            pagesRead = newPagesRead,
            status = newStatus,
            completedDate = completedDate,
            lastUpdated = Clock.System.now().toString()
        )
    }

    private fun incrementBooksRead() {
        val current = _readingGoal.value
        saveReadingGoal(current.copy(booksReadThisYear = current.booksReadThisYear + 1))
    }

    fun getCurrentlyReading(): List<Book> = _books.value.filter { it.status == ReadingStatus.READING }

    fun getToRead(): List<Book> = _books.value
        .filter { it.status == ReadingStatus.WANT }
        .sortedByDescending { it.priority.level }

    fun getCompleted(): List<Book> = _books.value
        .filter { it.status == ReadingStatus.DONE }
        .sortedByDescending { it.completedDate }

    fun getOtherActive(): List<Book> = emptyList()

    fun getTotalBooksCompleted(): Int = _books.value.count { it.status == ReadingStatus.DONE }
    
    fun getBooksReadThisYear(): Int {
        val currentYear = Clock.System.now().toString().substring(0, 4)
        return _books.value.count { 
            it.status == ReadingStatus.DONE && (it.completedDate?.startsWith(currentYear) == true)
        }
    }
}

data class ReadingGoal(
    val yearlyGoal: Int = 12,
    val monthlyGoal: Int = 1,
    val pagesPerDay: Int = 20,
    val booksReadThisYear: Int = 0,
    val currentStreak: Int = 0
)