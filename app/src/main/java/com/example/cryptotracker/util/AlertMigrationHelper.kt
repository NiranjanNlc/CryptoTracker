package com.example.cryptotracker.util

import android.content.Context
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.model.CryptoAlertProvider

/**
 * Helper class for migrating alerts from the old system to the new secure storage
 */
object AlertMigrationHelper {

    /**
     * Migrate alerts from the old system to the new secure storage
     *
     * @param context Application context
     * @param securePreferencesManager Secure preferences manager instance
     * @return True if migration was successful, false otherwise
     */
    fun migrateAlertsIfNeeded(context: Context, securePreferencesManager: SecurePreferencesManager): Boolean {
        // If we already have alerts in secure storage, no need to migrate
        if (securePreferencesManager.getAlerts().isNotEmpty()) {
            return true
        }

        try {
            // Get alerts from the old system (sample data in this case)
            val oldAlerts = CryptoAlertProvider.sampleAlerts
            
            // Convert old alerts to new format
            val newAlerts = AlertConverter.fromCryptoAlertList(oldAlerts)
            
            // Save to secure storage
            return securePreferencesManager.saveAlerts(newAlerts)
        } catch (e: Exception) {
            return false
        }
    }
}
