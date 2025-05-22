package com.example.cryptotracker.testing

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.worker.CryptoSimulationWorker
import com.example.cryptotracker.data.worker.SimulationManagerUtil
import com.example.cryptotracker.model.Alert
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Manual test utility for testing notification threshold crossing
 * This class provides methods to run manual tests for verifying that
 * price alerts trigger notifications correctly when thresholds are crossed.
 */
class NotificationThresholdManualTest(
    private val context: Context,
    private val repository: CryptoRepository,
    private val preferencesManager: com.example.cryptotracker.data.util.SecurePreferencesManager
) {
    private val TAG = "NotificationTest"
    private val simulationManager = SimulationManagerUtil(context, repository)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    /**
     * Test upper bound alert (price goes above threshold)
     * 
     * @param cryptoSymbol The symbol of the cryptocurrency to test (e.g., "BTC")
     * @param cryptoName The name of the cryptocurrency (e.g., "Bitcoin")
     * @param currentPrice The current price to start with
     * @param thresholdPercentage The percentage above current price to set the threshold
     */
    fun testUpperBoundAlert(
        cryptoSymbol: String,
        cryptoName: String,
        currentPrice: Double,
        thresholdPercentage: Double = 5.0
    ) {
        coroutineScope.launch {
            try {
                // Calculate threshold price (above current price)
                val thresholdPrice = currentPrice * (1 + thresholdPercentage / 100)
                
                // Create test cryptocurrency with initial price
                val testCrypto = createTestCrypto(cryptoSymbol, cryptoName, currentPrice)
                
                // Update repository with initial price
                updateRepositoryWithTestCrypto(testCrypto)
                
                // Create and save the alert
                val alert = createAlert(
                    cryptoSymbol = cryptoSymbol,
                    cryptoName = cryptoName,
                    threshold = thresholdPrice,
                    isUpperBound = true
                )
                
                showToast("Created UPPER bound alert for $cryptoName at $thresholdPrice")
                Log.i(TAG, "Created UPPER bound alert: current price = $currentPrice, threshold = $thresholdPrice")
                
                // Wait a moment before starting simulation
                delay(2000)
                
                // Run simulation with uptrend to cross the threshold
                showToast("Starting price simulation (UPTREND)...")
                Log.i(TAG, "Starting UPTREND simulation to trigger alert")
                
                // Run simulation with high volatility to ensure threshold crossing
                simulationManager.schedulePeriodicSimulation(
                    simulationMode = CryptoSimulationWorker.MODE_UPTREND,
                    volatility = 20.0f,
                    intervalMinutes = 1
                )
                
                showToast("Simulation running. Watch for notifications!")
                Log.i(TAG, "Simulation running. Check for notifications in the next minute.")
            } catch (e: Exception) {
                Log.e(TAG, "Error in upper bound test: ${e.message}")
                showToast("Test failed: ${e.message}")
            }
        }
    }
    
    /**
     * Test lower bound alert (price goes below threshold)
     * 
     * @param cryptoSymbol The symbol of the cryptocurrency to test (e.g., "BTC")
     * @param cryptoName The name of the cryptocurrency (e.g., "Bitcoin")
     * @param currentPrice The current price to start with
     * @param thresholdPercentage The percentage below current price to set the threshold
     */
    fun testLowerBoundAlert(
        cryptoSymbol: String,
        cryptoName: String,
        currentPrice: Double,
        thresholdPercentage: Double = 5.0
    ) {
        coroutineScope.launch {
            try {
                // Calculate threshold price (below current price)
                val thresholdPrice = currentPrice * (1 - thresholdPercentage / 100)
                
                // Create test cryptocurrency with initial price
                val testCrypto = createTestCrypto(cryptoSymbol, cryptoName, currentPrice)
                
                // Update repository with initial price
                updateRepositoryWithTestCrypto(testCrypto)
                
                // Create and save the alert
                val alert = createAlert(
                    cryptoSymbol = cryptoSymbol,
                    cryptoName = cryptoName,
                    threshold = thresholdPrice,
                    isUpperBound = false
                )
                
                showToast("Created LOWER bound alert for $cryptoName at $thresholdPrice")
                Log.i(TAG, "Created LOWER bound alert: current price = $currentPrice, threshold = $thresholdPrice")
                
                // Wait a moment before starting simulation
                delay(2000)
                
                // Run simulation with downtrend to cross the threshold
                showToast("Starting price simulation (DOWNTREND)...")
                Log.i(TAG, "Starting DOWNTREND simulation to trigger alert")
                
                // Run simulation with high volatility to ensure threshold crossing
                simulationManager.schedulePeriodicSimulation(
                    simulationMode = CryptoSimulationWorker.MODE_DOWNTREND,
                    volatility = 20.0f,
                    intervalMinutes = 1
                )
                
                showToast("Simulation running. Watch for notifications!")
                Log.i(TAG, "Simulation running. Check for notifications in the next minute.")
            } catch (e: Exception) {
                Log.e(TAG, "Error in lower bound test: ${e.message}")
                showToast("Test failed: ${e.message}")
            }
        }
    }
    
    /**
     * Stop all running simulations
     */
    fun stopAllSimulations() {
        coroutineScope.launch {
            simulationManager.cancelAllSimulations()
            showToast("All simulations stopped")
            Log.i(TAG, "All simulations stopped")
        }
    }
    
    /**
     * Clear all alerts
     */
    fun clearAllAlerts() {
        coroutineScope.launch {
            preferencesManager.clearAlerts()
            showToast("All alerts cleared")
            Log.i(TAG, "All alerts cleared")
        }
    }
    
    /**
     * Create a test cryptocurrency with specified properties
     */
    private fun createTestCrypto(
        symbol: String,
        name: String,
        price: Double
    ): CryptoCurrency {
        return CryptoCurrency(
            id = symbol.lowercase(),
            name = name,
            symbol = symbol,
            price = price,
            priceChangePercentage24h = 0.0,
            imageUrl = "https://example.com/${symbol.lowercase()}.png"
        )
    }
    
    /**
     * Update the repository with a test cryptocurrency
     */
    private suspend fun updateRepositoryWithTestCrypto(crypto: CryptoCurrency) {
        withContext(Dispatchers.IO) {
            repository.updateCachedPrices(listOf(crypto))
            Log.i(TAG, "Updated repository with test crypto: ${crypto.symbol} at ${crypto.price}")
        }
    }
    
    /**
     * Create and save an alert
     */
    private suspend fun createAlert(
        cryptoSymbol: String,
        cryptoName: String,
        threshold: Double,
        isUpperBound: Boolean
    ): Alert {
        return withContext(Dispatchers.IO) {
            val alert = Alert(
                id = UUID.randomUUID().toString(),
                cryptoSymbol = cryptoSymbol,
                cryptoName = cryptoName,
                threshold = threshold,
                isUpperBound = isUpperBound,
                isEnabled = true
            )
            
            preferencesManager.saveAlert(alert)
            Log.i(TAG, "Saved alert: $alert")
            
            alert
        }
    }
    
    /**
     * Show a toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
