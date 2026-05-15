package com.example.builddaily.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.builddaily.data.model.Book
import com.example.builddaily.data.model.BookGenre
import com.example.builddaily.data.model.BookPriority
import com.example.builddaily.data.model.BookStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
            booksReadThisYear = prefs.getInt("books_this_year", 0),
            currentStreak = prefs.getInt("reading_streak", 0)
        )
    }

    private fun saveReadingGoal(goal: ReadingGoal) {
        prefs.edit()
            .putInt("yearly_goal", goal.yearlyGoal)
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

    fun updateReadingProgress(id: String, pagesRead: Int) {
        val updated = _books.value.map {
            if (it.id == id) {
                val newStatus = if (pagesRead >= it.pages && it.pages > 0) BookStatus.COMPLETED else it.status
                val completedDate = if (newStatus == BookStatus.COMPLETED && it.completedDate == null) {
                    kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
                } else it.completedDate
                it.copy(pagesRead = pagesRead, status = newStatus, completedDate = completedDate)
            } else it
        }
        saveBooks(updated)
        if (updated.find { it.id == id }?.status == BookStatus.COMPLETED) {
            incrementBooksRead()
        }
    }

    private fun incrementBooksRead() {
        val current = _readingGoal.value
        saveReadingGoal(current.copy(booksReadThisYear = current.booksReadThisYear + 1))
    }

    fun setReadingGoal(yearlyGoal: Int) {
        val current = _readingGoal.value
        saveReadingGoal(current.copy(yearlyGoal = yearlyGoal))
    }

    fun updateReadingGoal(goal: ReadingGoal) {
        saveReadingGoal(goal)
    }

    fun getCurrentlyReading(): List<Book> = _books.value.filter { it.status == BookStatus.CURRENTLY_READING }

    fun getWantToRead(): List<Book> = _books.value
        .filter { it.status == BookStatus.WANT_TO_READ }
        .sortedByDescending { it.priority.level }

    fun getCompleted(): List<Book> = _books.value
        .filter { it.status == BookStatus.COMPLETED }
        .sortedByDescending { it.completedDate }

    fun getTotalBooksCompleted(): Int = _books.value.count { it.status == BookStatus.COMPLETED }
    
    fun getBooksReadThisYear(): Int {
        val currentYear = kotlinx.datetime.Clock.System.now().toString().substring(0, 4)
        return _books.value.count { 
            it.status == BookStatus.COMPLETED && (it.completedDate?.startsWith(currentYear) == true)
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