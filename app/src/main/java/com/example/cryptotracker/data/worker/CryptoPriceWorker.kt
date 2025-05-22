package com.example.cryptotracker.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.util.NotificationUtil
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.model.Alert

/**
 * Worker class that fetches cryptocurrency prices in the background,
 * updates the cached data every 5 minutes, and checks for price alerts
 */
class CryptoPriceWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val securePreferencesManager = SecurePreferencesManager(context)

    companion object {
        private const val TAG = "CryptoPriceWorker"
        
        // WorkManager unique work name
        const val WORK_NAME = "crypto_price_periodic_work"
    }

    /**
     * Performs the background work of fetching cryptocurrency prices,
     * updating the cache, and checking for price alerts
     */
    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting background price update and alert check")
        
        return try {
            // Get the repository from the application
            val repository = CryptoTrackerApplication.getRepository()
            
            // Fetch the latest prices, which will also update the cache
            val result = repository.getCryptoPrices()
            
            when (result) {
                is com.example.cryptotracker.data.util.Result.Success -> {
                    val cryptoList = result.data
                    Log.i(TAG, "Successfully updated prices in background, fetched ${cryptoList.size} cryptocurrencies")
                    
                    // Check for price alerts
                    checkPriceAlerts(cryptoList)
                    
                    Result.success()
                }
                is com.example.cryptotracker.data.util.Result.Error -> {
                    Log.e(TAG, "Error updating prices in background: ${result.message}")
                    // We still return success here because we don't want to retry immediately
                    // The next scheduled run will try again
                    Result.success()
                }
                is com.example.cryptotracker.data.util.Result.Loading -> {
                    Log.i(TAG, "Loading prices in background")
                    // We still return success here because we don't want to retry immediately
                    // The next scheduled run will try again
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during background price update: ${e.message}")
            // We still return success here because we don't want to retry immediately
            // The next scheduled run will try again
            Result.success()
        }
    }
    
    /**
     * Check if any price alerts have been triggered
     * 
     * @param cryptoList The list of cryptocurrencies with current prices
     */
    private fun checkPriceAlerts(cryptoList: List<com.example.cryptotracker.model.CryptoCurrency>) {
        try {
            // Get all stored alerts
            val alerts = securePreferencesManager.getAlerts()
            
            if (alerts.isEmpty()) {
                Log.d(TAG, "No alerts found to check")
                return
            }
            
            Log.d(TAG, "Checking ${alerts.size} alerts against current prices")
            
            // Create a map of crypto symbols to prices for faster lookup
            val priceMap = cryptoList.associateBy(
                { it.symbol.uppercase() },
                { it.price }
            )
            
            // Check each alert against the current price
            val triggeredAlerts = mutableListOf<Pair<Alert, Double>>()
            
            for (alert in alerts) {
                // Skip disabled alerts
                if (!alert.isEnabled) {
                    continue
                }
                
                // Get the current price for this crypto
                val currentPrice = priceMap[alert.cryptoSymbol.uppercase()]
                
                if (currentPrice != null) {
                    val isTriggered = if (alert.isUpperBound) {
                        // Alert triggers when price goes above threshold
                        currentPrice >= alert.threshold
                    } else {
                        // Alert triggers when price goes below threshold
                        currentPrice <= alert.threshold
                    }
                    
                    if (isTriggered) {
                        Log.i(TAG, "Alert triggered for ${alert.cryptoSymbol}: current price $currentPrice, threshold ${alert.threshold}")
                        triggeredAlerts.add(Pair(alert, currentPrice))
                    }
                }
            }
            
            // Send notifications for triggered alerts
            if (triggeredAlerts.isNotEmpty()) {
                Log.i(TAG, "Sending notifications for ${triggeredAlerts.size} triggered alerts")
                
                for ((alert, price) in triggeredAlerts) {
                    NotificationUtil.showAlertNotification(applicationContext, alert, price)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking price alerts: ${e.message}")
        }
    }
}
