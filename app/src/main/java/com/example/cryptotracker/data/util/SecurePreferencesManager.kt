package com.example.cryptotracker.data.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.cryptotracker.model.Alert
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Utility class for securely managing alert settings using EncryptedSharedPreferences
 */
class SecurePreferencesManager(context: Context) {

    private val gson = Gson()
    private val encryptedSharedPreferences: SharedPreferences

    init {
        try {
            // Create a simpler master key with default settings
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFERENCES_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            throw RuntimeException("Failed to initialize secure preferences: ${e.message}", e)
        }
    }

    /**
     * Save an alert to secure storage
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
            return false
        }
    }

    /**
     * Save a list of alerts to secure storage
     *
     * @param alerts List of alerts to save
     * @return True if successful, false otherwise
     */
    fun saveAlerts(alerts: List<Alert>): Boolean {
        return try {
            val jsonString = gson.toJson(alerts)
            encryptedSharedPreferences.edit()
                .putString(KEY_ALERTS, jsonString)
                .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get all alerts from secure storage
     *
     * @return List of alerts or empty list if none exist
     */
    fun getAlerts(): List<Alert> {
        val jsonString = encryptedSharedPreferences.getString(KEY_ALERTS, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<Alert>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
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
            return false
        }
    }

    /**
     * Clear all alerts from secure storage
     *
     * @return True if successful, false otherwise
     */
    fun clearAlerts(): Boolean {
        return try {
            encryptedSharedPreferences.edit()
                .remove(KEY_ALERTS)
                .remove(KEY_LAST_UPDATED)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get timestamp of when alerts were last updated
     *
     * @return Timestamp in milliseconds or 0 if no alerts exist
     */
    fun getLastUpdatedTimestamp(): Long {
        return encryptedSharedPreferences.getLong(KEY_LAST_UPDATED, 0)
    }

    companion object {
        private const val ENCRYPTED_PREFERENCES_NAME = "crypto_tracker_secure_preferences"
        private const val KEY_ALERTS = "secure_alerts"
        private const val KEY_LAST_UPDATED = "secure_last_updated"
    }
}
