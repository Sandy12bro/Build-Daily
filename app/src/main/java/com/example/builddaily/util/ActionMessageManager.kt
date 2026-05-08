package com.example.builddaily.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class ActionType {
    ADDED, UPDATED, DELETED, REPEATED, COMPLETED, INCOMPLETE
}

data class ActionMessage(
    val type: ActionType,
    val message: String
)

object ActionMessageManager {
    private val _messages = MutableSharedFlow<ActionMessage>(
        extraBufferCapacity = 5
    )
    val messages = _messages.asSharedFlow()

    fun postMessage(message: String, type: ActionType) {
        _messages.tryEmit(ActionMessage(type, message))
    }
}
