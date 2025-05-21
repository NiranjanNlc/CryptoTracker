package com.example.cryptotracker.util

import android.content.Context
import android.widget.Toast
import com.example.cryptotracker.data.util.PreferencesManager

/**
 * Utility class to help test the caching functionality
 */
object TestNetworkConnectivity {

    /**
     * Display instructions for testing the caching functionality
     *
     * @param context Application context
     */
    fun showTestInstructions(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val hasCachedData = preferencesManager.hasCachedData()
        
        val message = if (hasCachedData) {
            """
            Testing Instructions:
            
            1. You have cached data from ${formatTimestamp(preferencesManager.getLastUpdatedTimestamp())}
            
            2. To test offline mode:
               - Turn on Airplane Mode
               - Refresh the app to see cached data
               - Turn off Airplane Mode to fetch fresh data
               
            3. You can also clear the cache using the "Clear Cache" button
            """.trimIndent()
        } else {
            """
            Testing Instructions:
            
            1. You don't have any cached data yet
            
            2. First load data while online
            
            3. Then turn on Airplane Mode and refresh to test the offline cache
            """.trimIndent()
        }
        
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Format timestamp to readable date/time
     *
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date/time string
     */
    private fun formatTimestamp(timestamp: Long): String {
        return java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", 
            java.util.Locale.getDefault()
        ).format(java.util.Date(timestamp))
    }
}
