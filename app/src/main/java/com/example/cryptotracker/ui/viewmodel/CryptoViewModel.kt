package com.example.cryptotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.NetworkUtils
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException

/**
 * ViewModel for cryptocurrency data
 */
class CryptoViewModel(
    private val repository: CryptoRepository
) : ViewModel() {
    
    // UI state for cryptocurrency prices
    private val _cryptoListState = MutableStateFlow<CryptoListState>(CryptoListState.Loading)
    val cryptoListState: StateFlow<CryptoListState> = _cryptoListState.asStateFlow()
    
    // Track loading state to prevent multiple simultaneous loads
    private var isLoading = false
    
    init {
        // Load cryptocurrency prices when ViewModel is created
        loadCryptoPrices()
    }
    
    /**
     * Load cryptocurrency prices from repository
     * Includes error handling for different types of failures
     */
    fun loadCryptoPrices() {
        // Prevent multiple simultaneous loads
        if (isLoading) return
        
        viewModelScope.launch {
            isLoading = true
            _cryptoListState.value = CryptoListState.Loading
            
            try {
                when (val result = repository.getCryptoPrices()) {
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            _cryptoListState.value = CryptoListState.Error(
                                "No cryptocurrency data available"
                            )
                        } else {
                            _cryptoListState.value = CryptoListState.Success(result.data)
                        }
                    }
                    is Result.Error -> {
                        // Provide more specific error messages based on the exception
                        val errorMessage = when (result.exception) {
                            is IOException -> "Network error. Please check your connection and try again."
                            is HttpException -> "Server error. Please try again later."
                            else -> result.message ?: "Unknown error occurred"
                        }
                        _cryptoListState.value = CryptoListState.Error(errorMessage)
                    }
                    is Result.Loading -> {
                        _cryptoListState.value = CryptoListState.Loading
                    }
                }
            } catch (e: Exception) {
                // Catch any unexpected exceptions
                _cryptoListState.value = CryptoListState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Factory for creating CryptoViewModel
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
                return CryptoViewModel(
                    repository = CryptoTrackerApplication.getRepository()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Sealed class representing the state of cryptocurrency list
 */
sealed class CryptoListState {
    /**
     * Loading state
     */
    object Loading : CryptoListState()
    
    /**
     * Success state with data
     */
    data class Success(val data: List<CryptoCurrency>) : CryptoListState()
    
    /**
     * Error state with message
     */
    data class Error(val message: String) : CryptoListState()
}
