package com.example.cryptotracker.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Worker class that simulates changes in cryptocurrency prices
 * This is useful for testing price alerts and UI updates without relying on real API data
 */
class CryptoSimulationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        // Log tag
        private const val LOG_TAG = "CryptoSimulationWorker"
        
        // Tag for WorkManager queries
        const val TAG = "crypto_simulation"
        
        // WorkManager unique work name
        const val WORK_NAME = "crypto_simulation_work"
        
        // Input data keys
        const val KEY_SIMULATION_MODE = "simulation_mode"
        const val KEY_VOLATILITY = "volatility"
        
        // Simulation modes
        const val MODE_RANDOM = "random"           // Random price changes
        const val MODE_UPTREND = "uptrend"         // Generally increasing prices
        const val MODE_DOWNTREND = "downtrend"     // Generally decreasing prices
        const val MODE_VOLATILE = "volatile"       // Highly volatile price changes
        const val MODE_STABLE = "stable"           // Small price changes
        
        // Default volatility (percentage of price that can change)
        const val DEFAULT_VOLATILITY = 5.0f        // 5% volatility by default
        const val MAX_VOLATILITY = 20.0f           // Maximum 20% volatility
    }

    /**
     * Performs the simulation of cryptocurrency price changes
     */
    override suspend fun doWork(): Result {
        Log.i(LOG_TAG, "Starting cryptocurrency price simulation")
        
        // Get simulation parameters from input data
        val simulationMode = inputData.getString(KEY_SIMULATION_MODE) ?: MODE_RANDOM
        val volatility = inputData.getFloat(KEY_VOLATILITY, DEFAULT_VOLATILITY).coerceAtMost(MAX_VOLATILITY)
        
        Log.i(LOG_TAG, "Simulation mode: $simulationMode, Volatility: $volatility%")
        
        return try {
            // Get the repository from the application
            val repository = CryptoTrackerApplication.getRepository()
            
            // Get current crypto prices
            when (val result = repository.getCryptoPrices()) {
                is com.example.cryptotracker.data.util.Result.Success -> {
                    val cryptoList = result.data
                    Log.i(LOG_TAG, "Successfully fetched ${cryptoList.size} cryptocurrencies for simulation")
                    
                    // Simulate price changes
                    val simulatedCryptoList = simulatePriceChanges(cryptoList, simulationMode, volatility)
                    
                    // Update the repository with simulated prices
                    updateSimulatedPrices(repository, simulatedCryptoList)
                    
                    Result.success()
                }
                is com.example.cryptotracker.data.util.Result.Error -> {
                    Log.e(LOG_TAG, "Error fetching prices for simulation: ${result.message}")
                    Result.failure()
                }
                is com.example.cryptotracker.data.util.Result.Loading -> {
                    Log.i(LOG_TAG, "Loading prices for simulation")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception during price simulation: ${e.message}")
            Result.failure()
        }
    }
    
    /**
     * Simulate price changes based on the simulation mode and volatility
     * 
     * @param cryptoList The original list of cryptocurrencies
     * @param mode The simulation mode (random, uptrend, downtrend, volatile, stable)
     * @param volatilityPercent The maximum percentage by which prices can change
     * @return A list of cryptocurrencies with simulated prices
     */
    private fun simulatePriceChanges(
        cryptoList: List<CryptoCurrency>,
        mode: String,
        volatilityPercent: Float
    ): List<CryptoCurrency> {
        return cryptoList.map { crypto ->
            // Calculate the maximum price change based on volatility
            val maxPriceChange = crypto.price * (volatilityPercent / 100.0)
            
            // Calculate the price change based on the simulation mode
            val priceChange = when (mode) {
                MODE_UPTREND -> {
                    // In uptrend mode, prices generally increase (0% to volatility%)
                    Random.nextDouble(0.0, maxPriceChange)
                }
                MODE_DOWNTREND -> {
                    // In downtrend mode, prices generally decrease (0% to -volatility%)
                    -Random.nextDouble(0.0, maxPriceChange)
                }
                MODE_VOLATILE -> {
                    // In volatile mode, prices can change dramatically (-volatility% to +volatility%)
                    Random.nextDouble(-maxPriceChange, maxPriceChange)
                }
                MODE_STABLE -> {
                    // In stable mode, prices change very little (-volatility/4% to +volatility/4%)
                    Random.nextDouble(-maxPriceChange / 4, maxPriceChange / 4)
                }
                else -> {
                    // In random mode (default), prices can go up or down (-volatility/2% to +volatility/2%)
                    Random.nextDouble(-maxPriceChange / 2, maxPriceChange / 2)
                }
            }
            
            // Calculate the new price
            val newPrice = (crypto.price + priceChange).coerceAtLeast(0.01) // Ensure price is positive
            
            // Calculate a simulated 24h price change percentage
            val priceChangePercentage = when (mode) {
                MODE_UPTREND -> Random.nextDouble(0.0, volatilityPercent * 2.0)
                MODE_DOWNTREND -> -Random.nextDouble(0.0, volatilityPercent * 2.0)
                MODE_VOLATILE -> Random.nextDouble(-volatilityPercent * 3.0, volatilityPercent * 3.0)
             MODE_STABLE -> Random.nextDouble(-volatilityPercent.toDouble() / 2, volatilityPercent.toDouble() / 2)
                else -> Random.nextDouble(-volatilityPercent.toDouble(), volatilityPercent.toDouble())
            }
            
            // Create a new CryptoCurrency object with the simulated price
            crypto.copy(
                price = newPrice,
                priceChangePercentage24h = priceChangePercentage
            )
        }
    }
    
    /**
     * Update the repository with simulated prices
     * 
     * @param repository The repository to update
     * @param simulatedCryptoList The list of cryptocurrencies with simulated prices
     */
    private suspend fun updateSimulatedPrices(
        repository: com.example.cryptotracker.data.repository.CryptoRepository,
        simulatedCryptoList: List<CryptoCurrency>
    ) = withContext(Dispatchers.IO) {
        try {
            // Update the cache in the repository with simulated prices
            repository.updateCachedPrices(simulatedCryptoList)
            
            Log.i(LOG_TAG, "Successfully updated ${simulatedCryptoList.size} cryptocurrencies with simulated prices")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error updating simulated prices: ${e.message}")
        }
    }
}
