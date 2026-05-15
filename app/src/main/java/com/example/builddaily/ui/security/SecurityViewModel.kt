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

enum class SetupStep {
    VERIFY_OLD,
    CHOOSE_METHOD,
    ENTER_SECRET,
    CONFIRM_SECRET,
    SUCCESS
}

data class SecurityUiState(
    val input: String = "",
    val firstEntry: String = "",
    val setupStep: SetupStep = SetupStep.CHOOSE_METHOD,
    val selectedLockType: LockType? = null,
    val error: String? = null,
    val isUnlocked: Boolean = false,
    val failedAttempts: Int = 0,
    val isLockedOut: Boolean = false,
    val lockoutSecondsRemaining: Int = 0,
    val passwordStrength: Float = 0f, // 0 to 1
    val passwordStrengthLabel: String = ""
)

class SecurityViewModel(private val repository: SecurityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    val settings = repository.settings

    init {
        // Reset state on init to ensure clean setup flow
        _uiState.value = SecurityUiState()
    }

    fun startChangeFlow() {
        if (settings.value.isEnabled) {
            _uiState.value = SecurityUiState(setupStep = SetupStep.VERIFY_OLD)
        } else {
            _uiState.value = SecurityUiState(setupStep = SetupStep.CHOOSE_METHOD)
        }
    }

    fun selectLockType(type: LockType) {
        _uiState.value = _uiState.value.copy(
            selectedLockType = type,
            error = null
        )
    }

    fun startSetup() {
        if (_uiState.value.selectedLockType != null) {
            _uiState.value = _uiState.value.copy(
                setupStep = SetupStep.ENTER_SECRET,
                input = "",
                error = null
            )
        }
    }

    fun onInputChanged(input: String) {
        // Safety check for PIN lengths
        val state = _uiState.value
        val currentStep = state.setupStep
        val activeType = if (currentStep == SetupStep.VERIFY_OLD) settings.value.lockType else state.selectedLockType
        
        val maxLen = when(activeType) {
            LockType.PIN_4 -> 4
            LockType.PIN_6 -> 6
            else -> 50 // Password
        }
        
        if (input.length > maxLen) return

        // Only clear error if the user is actively typing a new attempt
        val newError = if (input.isEmpty()) _uiState.value.error else null
        _uiState.value = _uiState.value.copy(input = input, error = newError)
        
        val updatedState = _uiState.value
        if (currentStep == SetupStep.ENTER_SECRET && updatedState.selectedLockType == LockType.PASSWORD) {
            validatePasswordStrength(input)
        }

        // Auto-advance for PIN setup if length matches
        if (currentStep == SetupStep.ENTER_SECRET || currentStep == SetupStep.CONFIRM_SECRET || currentStep == SetupStep.VERIFY_OLD) {
            val targetLength = when(activeType) {
                LockType.PIN_4 -> 4
                LockType.PIN_6 -> 6
                else -> -1
            }
            
            if (targetLength != -1 && input.length == targetLength) {
                if (currentStep == SetupStep.VERIFY_OLD) {
                    verifyOldAndProceed()
                } else {
                    submitSetupInput()
                }
            }
        } else {
            // Unlocking logic
            val currentSettings = settings.value
            val expected = repository.getSecret()
            if (currentSettings.lockType == LockType.PIN_4 && input.length == 4) {
                validateUnlock(input, expected)
            } else if (currentSettings.lockType == LockType.PIN_6 && input.length == 6) {
                validateUnlock(input, expected)
            }
        }
    }

    fun onPatternComplete(pattern: String) {
        _uiState.value = _uiState.value.copy(input = pattern, error = null)
        val state = _uiState.value
        
        if (state.setupStep == SetupStep.ENTER_SECRET || state.setupStep == SetupStep.CONFIRM_SECRET) {
            if (pattern.split(",").size < 4) {
                _uiState.value = state.copy(error = "Draw at least 4 dots")
                return
            }
            submitSetupInput()
        } else if (state.setupStep == SetupStep.VERIFY_OLD) {
            verifyOldAndProceed()
        } else {
            val expected = repository.getSecret()
            validateUnlock(pattern, expected)
        }
    }

    private fun verifyOldAndProceed() {
        val input = _uiState.value.input
        val expected = repository.getSecret()
        if (input == expected) {
            _uiState.value = SecurityUiState(setupStep = SetupStep.CHOOSE_METHOD)
        } else {
            handleFailedAttempt()
        }
    }

    private fun validatePasswordStrength(password: String) {
        var score = 0f
        if (password.length >= 8) score += 0.25f
        if (password.any { it.isDigit() }) score += 0.25f
        if (password.any { it.isUpperCase() }) score += 0.25f
        if (password.any { !it.isLetterOrDigit() }) score += 0.25f
        
        val label = when {
            score < 0.25f -> "Very Weak"
            score < 0.5f -> "Weak"
            score < 0.75f -> "Medium"
            score < 1f -> "Strong"
            else -> "Very Strong"
        }
        _uiState.value = _uiState.value.copy(passwordStrength = score, passwordStrengthLabel = label)
    }

    fun submitSetupInput() {
        val state = _uiState.value
        when (state.setupStep) {
            SetupStep.VERIFY_OLD -> verifyOldAndProceed()
            SetupStep.ENTER_SECRET -> {
                if (state.input.isEmpty()) return
                if (state.selectedLockType == LockType.PASSWORD && state.passwordStrength < 0.5f) {
                    _uiState.value = state.copy(error = "Password is too weak")
                    return
                }
                _uiState.value = state.copy(
                    firstEntry = state.input,
                    input = "",
                    setupStep = SetupStep.CONFIRM_SECRET
                )
            }
            SetupStep.CONFIRM_SECRET -> {
                if (state.input == state.firstEntry) {
                    saveSecuritySetup()
                } else {
                    _uiState.value = state.copy(
                        error = "Credentials do not match. Enter correct password.",
                        input = ""
                    )
                }
            }
            else -> {}
        }
    }

    private fun saveSecuritySetup() {
        val state = _uiState.value
        repository.saveSecret(state.firstEntry)
        repository.updateSettings(settings.value.copy(
            isEnabled = true,
            lockType = state.selectedLockType ?: LockType.PIN_4
        ))
        _uiState.value = state.copy(setupStep = SetupStep.SUCCESS)
    }

    fun onPasswordSubmit() {
        if (_uiState.value.setupStep == SetupStep.ENTER_SECRET || _uiState.value.setupStep == SetupStep.CONFIRM_SECRET) {
            submitSetupInput()
        } else {
            val input = _uiState.value.input
            val expected = repository.getSecret()
            validateUnlock(input, expected)
        }
    }

    private fun validateUnlock(input: String, expected: String?) {
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

    fun resetSetup() {
        _uiState.value = SecurityUiState()
    }
}
