package com.example.cryptotracker.testing

import android.content.Context
import android.util.Log
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.data.worker.CryptoSimulationWorker
import com.example.cryptotracker.data.worker.SimulationManagerUtil
import com.example.cryptotracker.model.Alert
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Helper class to run notification tests programmatically
 * This class provides a simplified API for testing notification thresholds
 */
object NotificationTestRunner {
    private const val TAG = "NotificationTestRunner"
    
    /**
     * Run a complete notification test
     * 
     * @param context Application context
     * @param cryptoSymbol Symbol of the cryptocurrency (e.g., "BTC")
     * @param initialPrice Starting price for the test
     * @param targetPrice Target price that should trigger the notification
     * @param isUpperBound Whether this is an upper bound test (price goes above threshold)
     */
    fun runTest(
        context: Context,
        cryptoSymbol: String = "BTC",
        cryptoName: String = "Bitcoin",
        initialPrice: Double = 50000.0,
        targetPrice: Double = 52000.0,
        isUpperBound: Boolean = true
    ) {
        Log.i(TAG, "Starting notification test: $cryptoSymbol, initial: $initialPrice, target: $targetPrice, upperBound: $isUpperBound")
        
        val repository = CryptoTrackerApplication.getRepository()
        val preferencesManager = SecurePreferencesManager(context)
        val simulationManager = SimulationManagerUtil(context, repository)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1. Clear any existing alerts
                preferencesManager.clearAlerts()
                Log.i(TAG, "Cleared existing alerts")
                
                // 2. Create test cryptocurrency with initial price
                val testCrypto = createTestCrypto(cryptoSymbol, cryptoName, initialPrice)
                
                // 3. Update repository with initial price
                repository.updateCachedPrices(listOf(testCrypto))
                Log.i(TAG, "Set initial price for $cryptoSymbol to $initialPrice")
                
                // 4. Create and save the alert
                val alert = Alert(
                    id = UUID.randomUUID().toString(),
                    cryptoSymbol = cryptoSymbol,
                    cryptoName = cryptoName,
                    threshold = targetPrice,
                    isUpperBound = isUpperBound,
                    isEnabled = true
                )
                preferencesManager.saveAlert(alert)
                Log.i(TAG, "Created alert: threshold=$targetPrice, isUpperBound=$isUpperBound")
                
                // 5. Wait a moment before starting simulation
                delay(1000)
                
                // 6. Run simulation to cross the threshold
                val simulationMode = if (isUpperBound) {
                    CryptoSimulationWorker.MODE_UPTREND
                } else {
                    CryptoSimulationWorker.MODE_DOWNTREND
                }
                
                Log.i(TAG, "Starting simulation with mode: $simulationMode")
                simulationManager.schedulePeriodicSimulation(
                    simulationMode = simulationMode,
                    volatility = 20.0f,  // High volatility to ensure crossing the threshold
                    intervalMinutes = 1
                )
                
                Log.i(TAG, "Test running. Check for notifications in the next minute.")
            } catch (e: Exception) {
                Log.e(TAG, "Error running notification test: ${e.message}")
            }
        }
    }
    
    /**
     * Stop all running simulations
     */
    fun stopTest(context: Context) {
        val repository = CryptoTrackerApplication.getRepository()
        val simulationManager = SimulationManagerUtil(context, repository)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Cancel all simulations
                simulationManager.cancelAllSimulations()
                Log.i(TAG, "Stopped all simulations")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping test: ${e.message}")
            }
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
}
