package com.example.builddaily.util

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NotificationLog(
    val title: String,
    val description: String?,
    val timestamp: Long
)

object NotificationHistoryManager {
    private const val PREFS_NAME = "notification_history"
    private const val KEY_HISTORY = "history_logs"
    private val json = Json { ignoreUnknownKeys = true }

    fun addToHistory(context: Context, title: String, description: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentHistory = getHistory(context).toMutableList()
        
        currentHistory.add(0, NotificationLog(title, description, System.currentTimeMillis()))
        
        // Keep only last 20 entries
        val limitedHistory = currentHistory.take(20)
        
        prefs.edit().putString(KEY_HISTORY, json.encodeToString(limitedHistory)).apply()
    }

    fun getHistory(context: Context): List<NotificationLog> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyJson = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        return try {
            json.decodeFromString<List<NotificationLog>>(historyJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
