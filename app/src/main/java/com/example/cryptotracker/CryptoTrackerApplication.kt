package com.example.cryptotracker

import android.app.Application
import android.util.Log
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.FallbackPreferencesManager
import com.example.cryptotracker.data.util.NotificationUtil
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.data.worker.SimulationManagerUtil
import com.example.cryptotracker.data.worker.WorkManagerUtil
import com.example.cryptotracker.di.NetworkModule
import com.example.cryptotracker.util.AlertMigrationHelper

/**
 * Application class for CryptoTracker
 */
class CryptoTrackerApplication : Application() {
    
    // Lazy initialization of the repository
    val cryptoRepository: CryptoRepository by lazy {
        NetworkModule.provideCryptoRepository()
    }
    
    // Flag to track if we're using secure storage or fallback
    private var usingSecureStorage = true
    
    // Lazy initialization of the secure preferences manager with fallback
    val securePreferencesManager: SecurePreferencesManager by lazy {
        try {
            SecurePreferencesManager(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SecurePreferencesManager: ${e.message}", e)
            usingSecureStorage = false
            throw e
        }
    }
    
    // Fallback preferences manager when secure storage fails
    val fallbackPreferencesManager: FallbackPreferencesManager by lazy {
        FallbackPreferencesManager(applicationContext)
    }
    
    // Simulation manager utility
    val simulationManagerUtil: SimulationManagerUtil by lazy {
        SimulationManagerUtil(applicationContext, cryptoRepository)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize notification channel for price alerts
        NotificationUtil.createNotificationChannel(this)
        
        // Schedule periodic price updates using WorkManager
        WorkManagerUtil.schedulePriceUpdates(this)
        
        // Initialize simulation manager
        simulationManagerUtil
        
        try {
            // Try to initialize secure storage
            securePreferencesManager
            
            // If successful, migrate alerts
            try {
                AlertMigrationHelper.migrateAlertsIfNeeded(applicationContext, securePreferencesManager)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to migrate alerts: ${e.message}", e)
            }
        } catch (e: Exception) {
            // Secure storage failed, we'll use fallback
            Log.w(TAG, "Using fallback storage for alerts. Data will not be encrypted.")
        }
    }
    
    companion object {
        private const val TAG = "CryptoTrackerApp"
        
        // Singleton instance
        private lateinit var instance: CryptoTrackerApplication
        
        // Accessor for the repository
        fun getRepository(): CryptoRepository {
            return instance.cryptoRepository
        }
        
        // Accessor for preferences manager (secure or fallback)
        fun getSecurePreferencesManager(): SecurePreferencesManager {
            return try {
                instance.securePreferencesManager
            } catch (e: Exception) {
                // Return a wrapper that delegates to fallback
                throw RuntimeException("Secure storage is not available", e)
            }
        }
        
        // Check if secure storage is available
        fun isUsingSecureStorage(): Boolean {
            return instance.usingSecureStorage
        }
        
        // Get fallback preferences manager
        fun getFallbackPreferencesManager(): FallbackPreferencesManager {
            return instance.fallbackPreferencesManager
        }
        
        // Get simulation manager utility
        fun getSimulationManagerUtil(): SimulationManagerUtil {
            return instance.simulationManagerUtil
        }
        
        // Accessor for the application context
        fun getInstance(): CryptoTrackerApplication {
            return instance
        }
    }
}
