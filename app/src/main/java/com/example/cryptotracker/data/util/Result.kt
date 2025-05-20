package com.example.cryptotracker.data.util

/**
 * A generic class that holds a value or an error status
 * @param <T> Type of the value
 */
sealed class Result<out T> {
    /**
     * Success state with data
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * Error state with optional error message
     */
    data class Error(val message: String? = null, val exception: Exception? = null) : Result<Nothing>()
    
    /**
     * Loading state
     */
    object Loading : Result<Nothing>()
    
    /**
     * Check if the result is successful
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Check if the result is an error
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Check if the result is loading
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Get the data if the result is successful, otherwise return null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}
