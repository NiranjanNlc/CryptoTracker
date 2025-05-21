package com.example.cryptotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.model.CryptoAlert
import com.example.cryptotracker.model.CryptoAlertProvider
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for alert-related operations
 */
class AlertViewModel(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    
    // State for alerts list
    private val _alerts = MutableStateFlow<List<CryptoAlert>>(emptyList())
    val alerts: StateFlow<List<CryptoAlert>> = _alerts.asStateFlow()
    
    // State for available cryptocurrencies
    private val _availableCryptos = MutableStateFlow<List<CryptoCurrency>>(emptyList())
    val availableCryptos: StateFlow<List<CryptoCurrency>> = _availableCryptos.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Load initial data
        loadAlerts()
        loadAvailableCryptos()
    }
    
    /**
     * Load alerts from the provider
     */
    private fun loadAlerts() {
        // In a real app, this would load from a database or API
        // For now, we're using the sample data
        _alerts.value = CryptoAlertProvider.sampleAlerts
    }
    
    /**
     * Load available cryptocurrencies from the repository
     */
    private fun loadAvailableCryptos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = cryptoRepository.getCryptoPrices()
                if (result is com.example.cryptotracker.data.util.Result.Success) {
                    _availableCryptos.value = result.data.take(10) // Take top 10 cryptos
                } else if (result is com.example.cryptotracker.data.util.Result.Error) {
                    _error.value = result.message ?: "Failed to load cryptocurrencies"
                }
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Set an error message
     */
    fun setError(errorMessage: String?) {
        _error.value = errorMessage
    }
    
    /**
     * Clear the current error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Add a new alert
     */
    fun addAlert(crypto: CryptoCurrency, targetPrice: Double, isAboveTarget: Boolean) {
        val newAlert = CryptoAlert(
            id = UUID.randomUUID().toString(),
            cryptoName = crypto.name,
            cryptoSymbol = crypto.symbol,
            targetPrice = targetPrice,
            isAboveTarget = isAboveTarget
        )
        
        val currentAlerts = _alerts.value.toMutableList()
        currentAlerts.add(newAlert)
        _alerts.value = currentAlerts
        
        // In a real app, we would save this to a database or API
    }
    
    /**
     * Delete an alert
     */
    fun deleteAlert(alertId: String) {
        val currentAlerts = _alerts.value.toMutableList()
        currentAlerts.removeIf { it.id == alertId }
        _alerts.value = currentAlerts
        
        // In a real app, we would delete this from a database or API
    }
    
    /**
     * Toggle an alert's enabled state
     */
    fun toggleAlertEnabled(alertId: String, isEnabled: Boolean) {
        val currentAlerts = _alerts.value.toMutableList()
        val index = currentAlerts.indexOfFirst { it.id == alertId }
        
        if (index != -1) {
            currentAlerts[index] = currentAlerts[index].copy(isEnabled = isEnabled)
            _alerts.value = currentAlerts
            
            // In a real app, we would update this in a database or API
        }
    }
    
    /**
     * Factory for creating AlertViewModel
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
                return AlertViewModel(
                    cryptoRepository = CryptoTrackerApplication.getRepository()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
