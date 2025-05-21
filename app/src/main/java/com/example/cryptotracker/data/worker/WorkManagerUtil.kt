package com.example.cryptotracker.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Utility class for managing WorkManager tasks
 */
object WorkManagerUtil {
    private const val TAG = "WorkManagerUtil"
    
    // Interval for periodic price updates (5 minutes)
    private const val PRICE_UPDATE_INTERVAL_MINUTES = 5L
    
    /**
     * Schedule periodic price updates using WorkManager
     * Will replace any existing work with the same name
     * 
     * @param context Application context
     */
    fun schedulePriceUpdates(context: Context) {
        Log.i(TAG, "Scheduling periodic price updates every $PRICE_UPDATE_INTERVAL_MINUTES minutes")
        
        // Define constraints - we prefer to have network connectivity
        // but the Worker will handle offline scenarios with cached data
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Create a periodic work request
        val workRequest = PeriodicWorkRequestBuilder<CryptoPriceWorker>(
            PRICE_UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        // Enqueue the work request, replacing any existing one
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CryptoPriceWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Log.i(TAG, "Periodic price updates scheduled successfully")
    }
    
    /**
     * Cancel scheduled price updates
     * 
     * @param context Application context
     */
    fun cancelPriceUpdates(context: Context) {
        Log.i(TAG, "Cancelling scheduled price updates")
        WorkManager.getInstance(context).cancelUniqueWork(CryptoPriceWorker.WORK_NAME)
    }
}
