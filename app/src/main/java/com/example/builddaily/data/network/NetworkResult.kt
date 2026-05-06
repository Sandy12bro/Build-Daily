package com.example.builddaily.data.network

import kotlinx.serialization.Serializable

sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val exception: Throwable? = null) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
    
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (String, Throwable?) -> Unit): NetworkResult<T> {
        if (this is Error) action(message, exception)
        return this
    }
    
    inline fun onLoading(action: (Boolean) -> Unit): NetworkResult<T> {
        if (this is Loading) action(isLoading)
        return this
    }
}
