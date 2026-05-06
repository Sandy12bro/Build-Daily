package com.example.builddaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.di.RepositoryModule
import com.example.builddaily.data.network.NetworkResult
import com.example.builddaily.data.repository.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val authRepository = RepositoryModule.provideAuthRepository()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            when (val result = authRepository.signIn(email, password)) {
                is NetworkResult.Success -> {
                    _currentUser.value = result.data
                    _authError.value = null
                }
                is NetworkResult.Error -> {
                    _authError.value = result.message
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            when (val result = authRepository.signUp(email, password)) {
                is NetworkResult.Success -> {
                    _currentUser.value = result.data
                    _authError.value = null
                }
                is NetworkResult.Error -> {
                    _authError.value = result.message
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = authRepository.signOut()) {
                is NetworkResult.Success -> {
                    _currentUser.value = null
                    _authError.value = null
                }
                is NetworkResult.Error -> {
                    _authError.value = result.message
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _authError.value = null
    }
}
