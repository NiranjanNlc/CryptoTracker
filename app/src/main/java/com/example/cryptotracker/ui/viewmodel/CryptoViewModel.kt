package com.example.cryptotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for cryptocurrency data
 */
class CryptoViewModel(
    private val repository: CryptoRepository
) : ViewModel() {
    
    // UI state for cryptocurrency prices
    private val _cryptoListState = MutableStateFlow<CryptoListState>(CryptoListState.Loading)
    val cryptoListState: StateFlow<CryptoListState> = _cryptoListState.asStateFlow()
    
    init {
        // Load cryptocurrency prices when ViewModel is created
        loadCryptoPrices()
    }
    
    /**
     * Load cryptocurrency prices from repository
     */
    fun loadCryptoPrices() {
        viewModelScope.launch {
            _cryptoListState.value = CryptoListState.Loading
            
            when (val result = repository.getCryptoPrices()) {
                is Result.Success -> {
                    _cryptoListState.value = CryptoListState.Success(result.data)
                }
                is Result.Error -> {
                    _cryptoListState.value = CryptoListState.Error(
                        result.message ?: "Unknown error occurred"
                    )
                }
                is Result.Loading -> {
                    _cryptoListState.value = CryptoListState.Loading
                }
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
