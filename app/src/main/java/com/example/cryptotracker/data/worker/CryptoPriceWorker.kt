package com.example.cryptotracker.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.util.Result

/**
 * Worker class that fetches cryptocurrency prices in the background
 * and updates the cached data every 5 minutes
 */
class CryptoPriceWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "CryptoPriceWorker"
        
        // WorkManager unique work name
        const val WORK_NAME = "crypto_price_periodic_work"
    }

    /**
     * Performs the background work of fetching cryptocurrency prices
     * and updating the cache
     */
    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting background price update")
        
        return try {
            // Get the repository from the application
            val repository = CryptoTrackerApplication.getRepository()
            
            // Fetch the latest prices, which will also update the cache
            val result = repository.getCryptoPrices()
            
            when (result) {
                is com.example.cryptotracker.data.util.Result.Success -> {
                    Log.i(TAG, "Successfully updated prices in background, fetched ${result.data.size} cryptocurrencies")
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
}
