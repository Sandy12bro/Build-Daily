package com.example.builddaily.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.TodoItem
import com.example.builddaily.data.model.TodoPriority
import com.example.builddaily.data.repository.TodoListRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TodoListViewModel(private val repository: TodoListRepository) : ViewModel() {
    val todos: StateFlow<List<TodoItem>> = repository.todos

    fun addTodo(title: String, category: String, priority: TodoPriority, deadline: Long? = null) {
        if (title.isBlank()) return
        val newItem = TodoItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            priority = priority,
            deadline = deadline
        )
        viewModelScope.launch {
            repository.saveTodo(newItem)
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.saveTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }
}
