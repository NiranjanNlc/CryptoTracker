package com.example.cryptotracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.FallbackPreferencesManager
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.model.Alert
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
    private val cryptoRepository: CryptoRepository,
    private val securePreferencesManager: SecurePreferencesManager?,
    private val fallbackPreferencesManager: FallbackPreferencesManager?
) : ViewModel() {
    
    private val TAG = "AlertViewModel"
    
    // Flag to track which storage we're using
    private val useSecureStorage = securePreferencesManager != null
    
    // State for alerts list
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()
    
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
        loadAlerts()
        loadAvailableCryptos()
    }
    
    /**
     * Load alerts from storage
     */
    private fun loadAlerts() {
        try {
            val loadedAlerts = if (useSecureStorage) {
                securePreferencesManager?.getAlerts() ?: emptyList()
            } else {
                fallbackPreferencesManager?.getAlerts() ?: emptyList()
            }
            _alerts.value = loadedAlerts
        } catch (e: Exception) {
            Log.e(TAG, "Error loading alerts: ${e.message}", e)
            _alerts.value = emptyList()
        }
    }
    
    /**
     * Refresh alerts from storage
     */
    fun refreshAlerts() {
        loadAlerts()
    }
    
    /**
     * Load available cryptocurrencies
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
     *
     * @param crypto The cryptocurrency to alert on
     * @param targetPrice The target price threshold
     * @param isAboveTarget Whether to alert when price goes above target (true) or below (false)
     * @return Boolean indicating success or failure
     */
    fun addAlert(crypto: CryptoCurrency, targetPrice: Double, isAboveTarget: Boolean): Boolean {
        val newAlert = Alert(
            id = UUID.randomUUID().toString(),
            cryptoName = crypto.name,
            cryptoSymbol = crypto.symbol,
            threshold = targetPrice,
            isUpperBound = isAboveTarget
        )
        
        val success = if (useSecureStorage) {
            securePreferencesManager?.saveAlert(newAlert) ?: false
        } else {
            fallbackPreferencesManager?.saveAlert(newAlert) ?: false
        }
        
        if (success) {
            val currentAlerts = _alerts.value.toMutableList()
            currentAlerts.add(newAlert)
            _alerts.value = currentAlerts
        } else {
            Log.e(TAG, "Failed to save alert")
        }
        
        return success
    }
    
    /**
     * Delete an alert
     *
     * @param alertId The ID of the alert to delete
     * @return Boolean indicating success or failure
     */
    fun deleteAlert(alertId: String): Boolean {
        val success = if (useSecureStorage) {
            securePreferencesManager?.deleteAlert(alertId) ?: false
        } else {
            fallbackPreferencesManager?.deleteAlert(alertId) ?: false
        }
        
        if (success) {
            val currentAlerts = _alerts.value.toMutableList()
            currentAlerts.removeIf { it.id == alertId }
            _alerts.value = currentAlerts
        } else {
            Log.e(TAG, "Failed to delete alert")
        }
        
        return success
    }
    
    /**
     * Toggle an alert's enabled status
     *
     * @param alertId The ID of the alert to toggle
     * @param isEnabled The new enabled status
     * @return Boolean indicating success or failure
     */
    fun toggleAlertEnabled(alertId: String, isEnabled: Boolean): Boolean {
        // Find the alert in the current list
        val alertToUpdate = _alerts.value.find { it.id == alertId } ?: return false
        
        // Create updated alert with new enabled status
        val updatedAlert = alertToUpdate.copy(isEnabled = isEnabled)
        
        // Save to storage
        val success = if (useSecureStorage) {
            securePreferencesManager?.updateAlert(updatedAlert) ?: false
        } else {
            fallbackPreferencesManager?.updateAlert(updatedAlert) ?: false
        }
        
        if (success) {
            // Update in-memory list
            val currentAlerts = _alerts.value.toMutableList()
            val index = currentAlerts.indexOfFirst { it.id == alertId }
            if (index != -1) {
                currentAlerts[index] = updatedAlert
                _alerts.value = currentAlerts
            }
        } else {
            Log.e(TAG, "Failed to update alert enabled status")
        }
        
        return success
    }
    
    /**
     * Factory for creating AlertViewModel instances
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
                val app = CryptoTrackerApplication.getInstance()
                
                // Try to get secure preferences manager, fall back if not available
                val securePrefs = try {
                    CryptoTrackerApplication.getSecurePreferencesManager()
                } catch (e: Exception) {
                    null
                }
                
                // Get fallback preferences manager if secure storage is not available
                val fallbackPrefs = if (securePrefs == null) {
                    CryptoTrackerApplication.getFallbackPreferencesManager()
                } else {
                    null
                }
                
                return AlertViewModel(
                    CryptoTrackerApplication.getRepository(),
                    securePrefs,
                    fallbackPrefs
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
