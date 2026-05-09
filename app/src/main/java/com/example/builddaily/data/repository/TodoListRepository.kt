package com.example.builddaily.data.repository

import android.content.Context
import com.example.builddaily.data.model.TodoItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TodoListRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("todo_list_prefs", Context.MODE_PRIVATE)
    private val _todos = MutableStateFlow<List<TodoItem>>(loadTodos())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private fun loadTodos(): List<TodoItem> {
        val json = prefs.getString("todos", "[]") ?: "[]"
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveTodo(todo: TodoItem) {
        val current = _todos.value.toMutableList()
        val index = current.indexOfFirst { it.id == todo.id }
        if (index != -1) {
            current[index] = todo
        } else {
            current.add(0, todo)
        }
        
        // Notification management
        if (todo.isCompleted) {
            com.example.builddaily.util.TodoScheduler.cancelTodoReminder(context, todo.id)
        } else {
            com.example.builddaily.util.TodoScheduler.scheduleTodoReminder(context, todo)
        }
        
        updateTodos(current)
    }

    fun deleteTodo(id: String) {
        com.example.builddaily.util.TodoScheduler.cancelTodoReminder(context, id)
        val current = _todos.value.filter { it.id != id }
        updateTodos(current)
    }

    private fun updateTodos(list: List<TodoItem>) {
        _todos.value = list
        prefs.edit().putString("todos", Json.encodeToString(list)).apply()
    }
}
