package com.example.cryptotracker.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility class for network-related operations
 */
object NetworkUtils {
    
    // For testing purposes
    @Volatile
    private var isMockNetworkAvailable: Boolean? = null
    
    /**
     * Check if the device is connected to the internet
     * 
     * @param context Application context
     * @return True if connected, false otherwise
     */
    fun isNetworkAvailable(context: Context): Boolean {
        // For testing purposes
        isMockNetworkAvailable?.let {
            return it
        }
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Reset mock network availability (for testing)
     */
    fun resetMockNetworkAvailable() {
        isMockNetworkAvailable = null
    }
    
    /**
     * For testing purposes only - not to be used in production code
     */
    @JvmSynthetic // Hide from Java callers
    internal fun setMockNetworkAvailable(isAvailable: Boolean) {
        isMockNetworkAvailable = isAvailable
    }
}
