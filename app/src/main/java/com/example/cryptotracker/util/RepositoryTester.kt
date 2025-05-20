package com.example.cryptotracker.util

import android.util.Log
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.di.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility class to test the repository
 * This is for development purposes only and should be removed in production
 */
object RepositoryTester {
    private const val TAG = "RepositoryTester"
    
    /**
     * Test fetching cryptocurrency prices
     */
    fun testGetCryptoPrices() {
        val repository: CryptoRepository = NetworkModule.provideCryptoRepository()
        
        CoroutineScope(Dispatchers.Main).launch {
            when (val result = repository.getCryptoPrices()) {
                is Result.Success -> {
                    Log.d(TAG, "Successfully fetched ${result.data.size} cryptocurrencies")
                    result.data.forEach { crypto ->
                        Log.d(TAG, "Crypto: ${crypto.name} (${crypto.symbol}) - $${crypto.price}")
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error fetching cryptocurrencies: ${result.message}", result.exception)
                }
                is Result.Loading -> {
                    Log.d(TAG, "Loading cryptocurrencies...")
                }
            }
        }
    }
    
    /**
     * Test fetching a specific cryptocurrency by id
     * 
     * @param id Cryptocurrency id
     */
    fun testGetCryptoById(id: String) {
        val repository: CryptoRepository = NetworkModule.provideCryptoRepository()
        
        CoroutineScope(Dispatchers.Main).launch {
            when (val result = repository.getCryptoById(id)) {
                is Result.Success -> {
                    val crypto = result.data
                    if (crypto != null) {
                        Log.d(TAG, "Successfully fetched crypto: ${crypto.name} (${crypto.symbol}) - $${crypto.price}")
                    } else {
                        Log.d(TAG, "Cryptocurrency with id $id not found")
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error fetching cryptocurrency with id $id: ${result.message}", result.exception)
                }
                is Result.Loading -> {
                    Log.d(TAG, "Loading cryptocurrency with id $id...")
                }
            }
        }
    }
}
