package com.example.builddaily.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.security.LockType
import com.example.builddaily.data.security.SecurityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecurityViewModel(private val repository: SecurityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    val settings = repository.settings

    init {
        checkLockoutStatus()
    }

    private fun checkLockoutStatus() {
        // Implementation for failed attempts and cooldown
    }

    fun onInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(input = input, error = null)
        
        val settings = settings.value
        val expected = repository.getSecret()
        
        if (settings.lockType == LockType.PIN_4 && input.length == 4) {
            validate(input, expected)
        } else if (settings.lockType == LockType.PIN_6 && input.length == 6) {
            validate(input, expected)
        }
    }

    fun onPasswordSubmit() {
        val input = _uiState.value.input
        val expected = repository.getSecret()
        validate(input, expected)
    }

    private fun validate(input: String, expected: String?) {
        if (input == expected) {
            repository.markUnlocked()
            _uiState.value = _uiState.value.copy(isUnlocked = true)
        } else {
            handleFailedAttempt()
        }
    }

    private fun handleFailedAttempt() {
        val newAttempts = _uiState.value.failedAttempts + 1
        _uiState.value = _uiState.value.copy(
            failedAttempts = newAttempts,
            error = "Incorrect security credential",
            input = ""
        )

        if (newAttempts >= 5) {
            startLockout()
        }
    }

    private fun startLockout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLockedOut = true, lockoutSecondsRemaining = 30)
            while (_uiState.value.lockoutSecondsRemaining > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    lockoutSecondsRemaining = _uiState.value.lockoutSecondsRemaining - 1
                )
            }
            _uiState.value = _uiState.value.copy(isLockedOut = false, failedAttempts = 0)
        }
    }

    fun onBiometricSuccess() {
        repository.markUnlocked()
        _uiState.value = _uiState.value.copy(isUnlocked = true)
    }
}

data class SecurityUiState(
    val input: String = "",
    val error: String? = null,
    val isUnlocked: Boolean = false,
    val failedAttempts: Int = 0,
    val isLockedOut: Boolean = false,
    val lockoutSecondsRemaining: Int = 0
)
