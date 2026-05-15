package com.example.builddaily.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.*
import com.example.builddaily.data.repository.TodoListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class TodoListViewModel(
    private val repository: TodoListRepository,
    private val statsRepository: com.example.builddaily.data.repository.UserStatsRepository
) : ViewModel() {
    private val _todos = repository.todos
    
    private val _sortOption = MutableStateFlow(TodoSortOption.DEADLINE)
    val sortOption: StateFlow<TodoSortOption> = _sortOption.asStateFlow()

    val allTodos = _todos

    val activeTodos = combine(_todos, _sortOption) { todos, sort ->
        todos.filter { !it.isCompleted }.sortWithOption(sort)
    }

    val archivedTodos = _todos.combine(MutableStateFlow(Unit)) { todos, _ ->
        todos.filter { it.isCompleted }.sortedByDescending { it.completionTime ?: 0L }
    }

    fun setSortOption(option: TodoSortOption) {
        _sortOption.value = option
    }

    fun addTodo(
        title: String, 
        category: String, 
        priority: TodoPriority, 
        deadline: Long? = null,
        notes: String = "",
        tags: List<String> = emptyList(),
        hasReminder: Boolean = false
    ) {
        if (title.isBlank()) return
        val newItem = TodoItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            priority = priority,
            deadline = deadline,
            notes = notes,
            tags = tags,
            hasReminder = hasReminder,
            subtasks = emptyList()
        )
        viewModelScope.launch {
            repository.saveTodo(newItem)
        }
    }

    fun toggleTodo(todo: TodoItem) {
        if (todo.subtasks.isNotEmpty()) return

        val newStatus = !todo.isCompleted
        val completionTime = if (newStatus) System.currentTimeMillis() else null
        viewModelScope.launch {
            repository.saveTodo(todo.copy(isCompleted = newStatus, completionTime = completionTime))
            if (newStatus) statsRepository.addPoints(5)
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    fun restoreTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.saveTodo(todo.copy(isCompleted = false, completionTime = null))
        }
    }

    fun addSubTask(todo: TodoItem, title: String) {
        if (title.isBlank()) return
        val newSubTask = SubTask(UUID.randomUUID().toString(), title)
        val updatedSubtasks = todo.subtasks + newSubTask
        viewModelScope.launch {
            repository.saveTodo(todo.copy(
                subtasks = updatedSubtasks,
                isCompleted = false,
                completionTime = null
            ))
        }
    }

    fun toggleSubTask(todo: TodoItem, subTaskId: String) {
        val updatedSubtasks = todo.subtasks.map {
            if (it.id == subTaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        val allComplete = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.isCompleted }
        val completionTime = if (allComplete) System.currentTimeMillis() else null
        
        viewModelScope.launch {
            repository.saveTodo(todo.copy(
                subtasks = updatedSubtasks,
                isCompleted = allComplete,
                completionTime = completionTime
            ))
            if (allComplete && !todo.isCompleted) statsRepository.addPoints(5)
        }
    }

    fun deleteSubTask(todo: TodoItem, subTaskId: String) {
        val updatedSubtasks = todo.subtasks.filter { it.id != subTaskId }
        val allComplete = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.isCompleted }
        val completionTime = if (allComplete) System.currentTimeMillis() else null

        viewModelScope.launch {
            repository.saveTodo(todo.copy(
                subtasks = updatedSubtasks,
                isCompleted = allComplete,
                completionTime = completionTime
            ))
        }
    }

    fun updateTodo(
        todoId: String, 
        newTitle: String, 
        newCategory: String, 
        newPriority: TodoPriority, 
        newDeadline: Long?,
        newNotes: String = "",
        newTags: List<String> = emptyList(),
        newReminder: Boolean = false
    ) {
        if (newTitle.isBlank()) return
        val todo = _todos.value.find { it.id == todoId } ?: return
        viewModelScope.launch {
            repository.saveTodo(
                todo.copy(
                    title = newTitle,
                    category = newCategory,
                    priority = newPriority,
                    deadline = newDeadline,
                    notes = newNotes,
                    tags = newTags,
                    hasReminder = newReminder
                )
            )
        }
    }

    fun updateSubTaskTitle(todoId: String, subTaskId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        val todo = _todos.value.find { it.id == todoId } ?: return
        val updatedSubtasks = todo.subtasks.map {
            if (it.id == subTaskId) it.copy(title = newTitle) else it
        }
        viewModelScope.launch {
            repository.saveTodo(todo.copy(subtasks = updatedSubtasks))
        }
    }

    private fun List<TodoItem>.sortWithOption(option: TodoSortOption): List<TodoItem> {
        return when (option) {
            TodoSortOption.DEADLINE -> sortedWith(
                compareBy<TodoItem> { it.deadline == null }
                    .thenBy { it.deadline ?: Long.MAX_VALUE }
            )
            TodoSortOption.PRIORITY -> sortedByDescending { it.priority.ordinal }
            TodoSortOption.CREATED_NEWEST -> sortedByDescending { it.createdAt }
            TodoSortOption.CREATED_OLDEST -> sortedBy { it.createdAt }
            TodoSortOption.ALPHABETICAL -> sortedBy { it.title.lowercase() }
            TodoSortOption.PROGRESS -> sortedByDescending { 
                if (it.subtasks.isEmpty()) 0f else it.subtasks.count { st -> st.isCompleted }.toFloat() / it.subtasks.size 
            }
        }
    }
}
