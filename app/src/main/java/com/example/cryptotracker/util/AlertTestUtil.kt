package com.example.cryptotracker.util

import android.content.Context
import android.util.Log
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.model.Alert
import java.util.UUID

/**
 * Utility class for testing alert functionality
 */
object AlertTestUtil {
    private const val TAG = "AlertTestUtil"
    
    /**
     * Create a sample Bitcoin alert for testing
     * 
     * @param context Application context
     * @return true if the alert was successfully created
     */
    fun createSampleBitcoinAlert(context: Context): Boolean {
        return try {
            val securePreferencesManager = SecurePreferencesManager(context)
            
            // Create a sample Bitcoin alert that triggers when price goes above $70,000
            val sampleAlert = Alert(
                id = UUID.randomUUID().toString(),
                cryptoSymbol = "BTC",
                cryptoName = "Bitcoin",
                threshold = 70000.0,
                isUpperBound = true,
                isEnabled = true
            )
            
            val result = securePreferencesManager.saveAlert(sampleAlert)
            
            if (result) {
                Log.i(TAG, "Created sample Bitcoin alert: price > $70,000")
            } else {
                Log.e(TAG, "Failed to create sample Bitcoin alert")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error creating sample alert: ${e.message}")
            false
        }
    }
    
    /**
     * Create multiple sample alerts for testing
     * 
     * @param context Application context
     * @return true if all alerts were successfully created
     */
    fun createSampleAlerts(context: Context): Boolean {
        return try {
            val securePreferencesManager = SecurePreferencesManager(context)
            
            // Create a list of sample alerts
            val sampleAlerts = listOf(
                Alert(
                    id = UUID.randomUUID().toString(),
                    cryptoSymbol = "BTC",
                    cryptoName = "Bitcoin",
                    threshold = 70000.0,
                    isUpperBound = true,
                    isEnabled = true
                ),
                Alert(
                    id = UUID.randomUUID().toString(),
                    cryptoSymbol = "ETH",
                    cryptoName = "Ethereum",
                    threshold = 3000.0,
                    isUpperBound = true,
                    isEnabled = true
                ),
                Alert(
                    id = UUID.randomUUID().toString(),
                    cryptoSymbol = "BTC",
                    cryptoName = "Bitcoin",
                    threshold = 50000.0,
                    isUpperBound = false,
                    isEnabled = true
                )
            )
            
            val result = securePreferencesManager.saveAlerts(sampleAlerts)
            
            if (result) {
                Log.i(TAG, "Created ${sampleAlerts.size} sample alerts for testing")
            } else {
                Log.e(TAG, "Failed to create sample alerts")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error creating sample alerts: ${e.message}")
            false
        }
    }
    
    /**
     * Clear all alerts
     * 
     * @param context Application context
     * @return true if all alerts were successfully cleared
     */
    fun clearAllAlerts(context: Context): Boolean {
        return try {
            val securePreferencesManager = SecurePreferencesManager(context)
            val result = securePreferencesManager.clearAlerts()
            
            if (result) {
                Log.i(TAG, "Cleared all alerts")
            } else {
                Log.e(TAG, "Failed to clear alerts")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing alerts: ${e.message}")
            false
        }
    }
}
