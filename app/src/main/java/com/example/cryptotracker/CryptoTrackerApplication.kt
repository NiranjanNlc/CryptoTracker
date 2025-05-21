package com.example.cryptotracker

import android.app.Application
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.di.NetworkModule

/**
 * Application class for CryptoTracker
 */
class CryptoTrackerApplication : Application() {
    
    // Lazy initialization of the repository
    val cryptoRepository: CryptoRepository by lazy {
        NetworkModule.provideCryptoRepository()
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        // Singleton instance
        private lateinit var instance: CryptoTrackerApplication
        
        // Accessor for the repository
        fun getRepository(): CryptoRepository {
            return instance.cryptoRepository
        }
        
        // Accessor for the application context
        fun getInstance(): CryptoTrackerApplication {
            return instance
        }
    }
}
