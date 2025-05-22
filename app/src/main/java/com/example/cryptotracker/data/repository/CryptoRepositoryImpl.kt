package com.example.cryptotracker.data.repository

import android.util.Log
import com.example.cryptotracker.data.api.CoinGeckoApi
import com.example.cryptotracker.data.model.toDomainModel
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

/**
 * Implementation of [CryptoRepository] that fetches data from CoinGecko API
 */
class CryptoRepositoryImpl(
    private val coinGeckoApi: CoinGeckoApi
) : CryptoRepository {
    
    // Cache for cryptocurrency data
    private val cryptoCache = AtomicReference<List<CryptoCurrency>>(emptyList())
    
    // Timestamp of last cache update
    private var lastCacheUpdateTime: Long = 0
    
    /**
     * Get real-time cryptocurrency prices from CoinGecko API
     * 
     * @return Result containing list of cryptocurrency data
     */
    override suspend fun getCryptoPrices(): Result<List<CryptoCurrency>> = withContext(Dispatchers.IO) {
        try {
            val response = coinGeckoApi.getCryptoMarkets()
            Log.i("CryptoRepository", "Fetched ${response.size} cryptocurrencies")
            val cryptoList = response.toDomainModel()
            
            // Update cache with the fetched data
            updateCache(cryptoList)
            
            Result.Success(cryptoList)
        } catch (e: Exception) {
            Log.e("CryptoRepository", "Error fetching cryptocurrencies: ${e.message}")
            
            // If we have cached data, return it instead
            val cachedData = cryptoCache.get()
            if (cachedData.isNotEmpty()) {
                Log.i("CryptoRepository", "Returning ${cachedData.size} cryptocurrencies from cache")
                return@withContext Result.Success(cachedData)
            }
            
            Result.Error("Failed to fetch cryptocurrency prices", e)
        }
    }
    
    /**
     * Get cryptocurrency by id
     * 
     * @param id Cryptocurrency id
     * @return Result containing cryptocurrency data
     */
    override suspend fun getCryptoById(id: String): Result<CryptoCurrency?> = withContext(Dispatchers.IO) {
        try {
            // First check the cache
            val cachedData = cryptoCache.get()
            val cachedCrypto = cachedData.find { it.id == id }
            
            if (cachedCrypto != null) {
                Log.i("CryptoRepository", "Returning cryptocurrency $id from cache")
                return@withContext Result.Success(cachedCrypto)
            }
            
            // If not in cache, fetch from API
            val cryptoList = coinGeckoApi.getCryptoMarkets()
            val crypto = cryptoList.find { it.id == id }?.toDomainModel()
            
            // Update cache with the fetched data
            if (crypto != null) {
                val updatedCache = cachedData.toMutableList()
                val existingIndex = updatedCache.indexOfFirst { it.id == id }
                
                if (existingIndex >= 0) {
                    updatedCache[existingIndex] = crypto
                } else {
                    updatedCache.add(crypto)
                }
                
                updateCache(updatedCache)
            }
            
            Result.Success(crypto)
        } catch (e: Exception) {
            // If we have cached data, check it
            val cachedData = cryptoCache.get()
            val cachedCrypto = cachedData.find { it.id == id }
            
            if (cachedCrypto != null) {
                Log.i("CryptoRepository", "Returning cryptocurrency $id from cache after API error")
                return@withContext Result.Success(cachedCrypto)
            }
            
            Result.Error("Failed to fetch cryptocurrency with id: $id", e)
        }
    }
    
    /**
     * Update the cached cryptocurrency prices
     * This is primarily used for simulation purposes
     * 
     * @param cryptoList List of cryptocurrencies with updated prices
     */
    override suspend fun updateCachedPrices(cryptoList: List<CryptoCurrency>) = withContext(Dispatchers.IO) {
        Log.i("CryptoRepository", "Updating cache with ${cryptoList.size} simulated cryptocurrencies")
        updateCache(cryptoList)
    }
    
    /**
     * Update the internal cache with new data
     * 
     * @param cryptoList List of cryptocurrencies to cache
     */
    private fun updateCache(cryptoList: List<CryptoCurrency>) {
        cryptoCache.set(cryptoList)
        lastCacheUpdateTime = System.currentTimeMillis()
        Log.d("CryptoRepository", "Cache updated with ${cryptoList.size} cryptocurrencies")
    }
}
