package com.example.cryptotracker.data.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cryptotracker.model.Alert
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Fallback implementation of preferences manager when secure storage is unavailable
 * Uses regular SharedPreferences with a warning about security implications
 */
class FallbackPreferencesManager(context: Context) {
    
    private val TAG = "FallbackPrefsManager"
    private val gson = Gson()
    private val sharedPreferences: SharedPreferences
    
    init {
        Log.w(TAG, "Using insecure storage for alerts. Sensitive data will not be encrypted.")
        sharedPreferences = context.getSharedPreferences(
            PREFERENCES_NAME, Context.MODE_PRIVATE
        )
    }
    
    /**
     * Save an alert to storage
     *
     * @param alert Alert to save
     * @return True if successful, false otherwise
     */
    fun saveAlert(alert: Alert): Boolean {
        try {
            val alerts = getAlerts().toMutableList()
            // Remove existing alert with same ID if it exists
            alerts.removeIf { it.id == alert.id }
            // Add the new alert
            alerts.add(alert)
            // Save the updated list
            return saveAlerts(alerts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save alert: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Save a list of alerts to storage
     *
     * @param alerts List of alerts to save
     * @return True if successful, false otherwise
     */
    fun saveAlerts(alerts: List<Alert>): Boolean {
        return try {
            val jsonString = gson.toJson(alerts)
            sharedPreferences.edit()
                .putString(KEY_ALERTS, jsonString)
                .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                .apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save alerts: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get all alerts from storage
     *
     * @return List of alerts or empty list if none exist
     */
    fun getAlerts(): List<Alert> {
        val jsonString = sharedPreferences.getString(KEY_ALERTS, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<Alert>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse alerts: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Delete an alert by ID
     *
     * @param alertId ID of the alert to delete
     * @return True if successful, false otherwise
     */
    fun deleteAlert(alertId: String): Boolean {
        try {
            val alerts = getAlerts().toMutableList()
            val initialSize = alerts.size
            alerts.removeIf { it.id == alertId }
            
            // If no alert was removed, return false
            if (alerts.size == initialSize) {
                return false
            }
            
            return saveAlerts(alerts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete alert: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Update an alert's enabled status
     *
     * @param alertId ID of the alert to update
     * @param isEnabled New enabled status
     * @return True if successful, false otherwise
     */
    fun updateAlertStatus(alertId: String, isEnabled: Boolean): Boolean {
        try {
            val alerts = getAlerts().toMutableList()
            val index = alerts.indexOfFirst { it.id == alertId }
            
            if (index == -1) {
                return false
            }
            
            alerts[index] = alerts[index].copy(isEnabled = isEnabled)
            return saveAlerts(alerts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update alert status: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Update an entire alert object
     *
     * @param alert The updated alert object
     * @return True if successful, false otherwise
     */
    fun updateAlert(alert: Alert): Boolean {
        try {
            val alerts = getAlerts().toMutableList()
            val index = alerts.indexOfFirst { it.id == alert.id }
            
            if (index == -1) {
                return false
            }
            
            alerts[index] = alert
            return saveAlerts(alerts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update alert: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Clear all alerts from storage
     *
     * @return True if successful, false otherwise
     */
    fun clearAlerts(): Boolean {
        return try {
            sharedPreferences.edit()
                .remove(KEY_ALERTS)
                .remove(KEY_LAST_UPDATED)
                .apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear alerts: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get timestamp of when alerts were last updated
     *
     * @return Timestamp in milliseconds or 0 if no alerts exist
     */
    fun getLastUpdatedTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LAST_UPDATED, 0)
    }
    
    companion object {
        private const val PREFERENCES_NAME = "crypto_tracker_fallback_preferences"
        private const val KEY_ALERTS = "alerts"
        private const val KEY_LAST_UPDATED = "last_updated"
    }
}
