package com.example.builddaily.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.TodoItem
import com.example.builddaily.data.model.TodoPriority
import com.example.builddaily.data.repository.TodoListRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TodoListViewModel(
    private val repository: TodoListRepository,
    private val statsRepository: com.example.builddaily.data.repository.UserStatsRepository
) : ViewModel() {
    val todos: StateFlow<List<TodoItem>> = repository.todos

    fun addTodo(title: String, category: String, priority: TodoPriority, deadline: Long? = null) {
        if (title.isBlank()) return
        val newItem = TodoItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            priority = priority,
            deadline = deadline,
            subtasks = emptyList()
        )
        viewModelScope.launch {
            repository.saveTodo(newItem)
        }
    }

    fun toggleTodo(todo: TodoItem) {
        val newStatus = !todo.isCompleted
        viewModelScope.launch {
            repository.saveTodo(todo.copy(isCompleted = newStatus))
            if (newStatus) {
                statsRepository.addPoints(5) // Smaller bonus for quick todos
            }
        }
    }

    fun addSubTask(todoId: String, title: String) {
        if (title.isBlank()) return
        val todo = todos.value.find { it.id == todoId } ?: return
        val newSubTask = com.example.builddaily.data.model.SubTask(
            id = UUID.randomUUID().toString(),
            title = title,
            isCompleted = false
        )
        val updatedSubtasks = todo.subtasks + newSubTask
        viewModelScope.launch {
            repository.saveTodo(todo.copy(subtasks = updatedSubtasks))
        }
    }

    fun toggleSubTask(todoId: String, subTaskId: String) {
        val todo = todos.value.find { it.id == todoId } ?: return
        val updatedSubtasks = todo.subtasks.map {
            if (it.id == subTaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        viewModelScope.launch {
            repository.saveTodo(todo.copy(subtasks = updatedSubtasks))
        }
    }

    fun deleteSubTask(todoId: String, subTaskId: String) {
        val todo = todos.value.find { it.id == todoId } ?: return
        val updatedSubtasks = todo.subtasks.filter { it.id != subTaskId }
        viewModelScope.launch {
            repository.saveTodo(todo.copy(subtasks = updatedSubtasks))
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    fun updateTodo(todoId: String, newTitle: String, newCategory: String, newPriority: TodoPriority, newDeadline: Long?) {
        if (newTitle.isBlank()) return
        val todo = todos.value.find { it.id == todoId } ?: return
        viewModelScope.launch {
            repository.saveTodo(
                todo.copy(
                    title = newTitle,
                    category = newCategory,
                    priority = newPriority,
                    deadline = newDeadline
                )
            )
        }
    }

    fun updateSubTaskTitle(todoId: String, subTaskId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        val todo = todos.value.find { it.id == todoId } ?: return
        val updatedSubtasks = todo.subtasks.map {
            if (it.id == subTaskId) it.copy(title = newTitle) else it
        }
        viewModelScope.launch {
            repository.saveTodo(todo.copy(subtasks = updatedSubtasks))
        }
    }
}
