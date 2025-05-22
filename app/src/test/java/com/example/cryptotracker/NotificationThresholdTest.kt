package com.example.cryptotracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.NotificationUtil
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.data.worker.CryptoSimulationWorker
import com.example.cryptotracker.data.worker.SimulationManagerUtil
import com.example.cryptotracker.model.Alert
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import java.util.UUID

/**
 * Test for verifying that price alerts trigger notifications correctly when thresholds are crossed
 */
@RunWith(AndroidJUnit4::class)
class NotificationThresholdTest {

    private lateinit var context: Context
    private lateinit var repository: CryptoRepository
    private lateinit var preferencesManager: SecurePreferencesManager
    private lateinit var simulationManager: SimulationManagerUtil
    private lateinit var notificationUtilMock: NotificationUtil

    // Test cryptocurrency
    private val testCrypto = CryptoCurrency(
        id = "bitcoin",
        name = "Bitcoin",
        symbol = "BTC",
        price = 50000.0,
        marketCap = 1000000000000.0,
        volume24h = 50000000000.0,
        priceChangePercentage24h = 2.5,
        circulatingSupply = 19000000.0,
        totalSupply = 21000000.0,
        maxSupply = 21000000.0,
        imageUrl = "https://example.com/bitcoin.png"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Mock repository
        repository = mock()
        
        // Mock preferences manager
        preferencesManager = mock()
        
        // Create simulation manager with mocked dependencies
        simulationManager = SimulationManagerUtil(context, repository)
        
        // Mock NotificationUtil as a singleton
        notificationUtilMock = mock()
        
        // Clear any existing alerts
        Mockito.`when`(preferencesManager.clearAlerts()).thenReturn(true)
        preferencesManager.clearAlerts()
    }

    @After
    fun tearDown() {
        // Clean up alerts
        preferencesManager.clearAlerts()
        
        // Cancel any ongoing simulations
        simulationManager.cancelAllSimulations()
    }

    @Test
    fun testUpperBoundAlertTriggersNotification() = runBlocking {
        // Create an upper bound alert (triggers when price goes above threshold)
        val upperBoundAlert = Alert(
            id = UUID.randomUUID().toString(),
            cryptoSymbol = "BTC",
            cryptoName = "Bitcoin",
            threshold = 52000.0,  // Set threshold above current price
            isUpperBound = true,
            isEnabled = true
        )
        
        // Mock saving the alert
        Mockito.`when`(preferencesManager.saveAlert(any())).thenReturn(true)
        preferencesManager.saveAlert(upperBoundAlert)
        
        // Mock getting alerts
        Mockito.`when`(preferencesManager.getAlerts()).thenReturn(listOf(upperBoundAlert))
        
        // Create simulated price data that crosses the threshold
        val simulatedCrypto = testCrypto.copy(price = 53000.0)  // Price above threshold
        
        // Mock repository to return our simulated data
        Mockito.`when`(repository.getCryptoPrices()).thenReturn(
            com.example.cryptotracker.data.util.Result.Success(listOf(simulatedCrypto))
        )
        
        // Mock the notification method
        Mockito.`when`(NotificationUtil.showAlertNotification(any(), any(), anyDouble())).thenReturn(true)
        
        // Trigger the simulation with uptrend mode to increase prices
        simulationManager.runOneTimeSimulation(
            simulationMode = CryptoSimulationWorker.MODE_UPTREND,
            volatility = 10.0f  // 10% volatility should be enough to cross the threshold
        )
        
        // Wait for simulation to complete
        Thread.sleep(2000)
        
        // Verify that the notification was triggered
        verify(NotificationUtil, times(1)).showAlertNotification(any(), any(), anyDouble())
    }

    @Test
    fun testLowerBoundAlertTriggersNotification() = runBlocking {
        // Create a lower bound alert (triggers when price goes below threshold)
        val lowerBoundAlert = Alert(
            id = UUID.randomUUID().toString(),
            cryptoSymbol = "BTC",
            cryptoName = "Bitcoin",
            threshold = 48000.0,  // Set threshold below current price
            isUpperBound = false,
            isEnabled = true
        )
        
        // Mock saving the alert
        Mockito.`when`(preferencesManager.saveAlert(any())).thenReturn(true)
        preferencesManager.saveAlert(lowerBoundAlert)
        
        // Mock getting alerts
        Mockito.`when`(preferencesManager.getAlerts()).thenReturn(listOf(lowerBoundAlert))
        
        // Create simulated price data that crosses the threshold
        val simulatedCrypto = testCrypto.copy(price = 47000.0)  // Price below threshold
        
        // Mock repository to return our simulated data
        Mockito.`when`(repository.getCryptoPrices()).thenReturn(
            com.example.cryptotracker.data.util.Result.Success(listOf(simulatedCrypto))
        )
        
        // Mock the notification method
        Mockito.`when`(NotificationUtil.showAlertNotification(any(), any(), anyDouble())).thenReturn(true)
        
        // Trigger the simulation with downtrend mode to decrease prices
        simulationManager.runOneTimeSimulation(
            simulationMode = CryptoSimulationWorker.MODE_DOWNTREND,
            volatility = 10.0f  // 10% volatility should be enough to cross the threshold
        )
        
        // Wait for simulation to complete
        Thread.sleep(2000)
        
        // Verify that the notification was triggered
        verify(NotificationUtil, times(1)).showAlertNotification(any(), any(), anyDouble())
    }
}
