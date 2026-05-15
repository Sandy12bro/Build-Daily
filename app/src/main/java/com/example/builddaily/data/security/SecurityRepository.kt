package com.example.builddaily.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityRepository(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SecuritySettings> = _settings.asStateFlow()

    private fun loadSettings(): SecuritySettings {
        val json = sharedPreferences.getString("security_settings", null)
        return if (json != null) {
            try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                SecuritySettings()
            }
        } else {
            SecuritySettings()
        }
    }

    fun updateSettings(newSettings: SecuritySettings) {
        val json = Json.encodeToString(newSettings)
        sharedPreferences.edit().putString("security_settings", json).apply()
        _settings.value = newSettings
    }

    fun saveSecret(secret: String) {
        sharedPreferences.edit().putString("app_lock_secret", secret).apply()
    }

    fun getSecret(): String? {
        return sharedPreferences.getString("app_lock_secret", null)
    }

    fun clearSecret() {
        sharedPreferences.edit().remove("app_lock_secret").apply()
    }

    fun isAppLocked(): Boolean {
        val s = settings.value
        if (!s.isEnabled || s.lockType == LockType.NONE) return false
        
        if (s.autoLockTimeoutMinutes == 0) return true
        
        val now = System.currentTimeMillis()
        val elapsedMillis = now - s.lastUnlockTime
        return elapsedMillis > (s.autoLockTimeoutMinutes * 60 * 1000)
    }

    fun markUnlocked() {
        val s = settings.value
        updateSettings(s.copy(lastUnlockTime = System.currentTimeMillis()))
    }
}
