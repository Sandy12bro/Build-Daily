package com.example.builddaily.data.security

import kotlinx.serialization.Serializable

@Serializable
enum class LockType {
    NONE,
    PIN_4,
    PIN_6,
    PATTERN,
    PASSWORD
}

@Serializable
data class SecuritySettings(
    val isEnabled: Boolean = false,
    val lockType: LockType = LockType.NONE,
    val isBiometricEnabled: Boolean = false,
    val autoLockTimeoutMinutes: Int = 0, // 0 means instantly
    val lastUnlockTime: Long = 0,
    val isScreenshotBlockingEnabled: Boolean = false,
    val isStealthModeEnabled: Boolean = false
)
