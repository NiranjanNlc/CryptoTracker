package com.example.cryptotracker.data.repository

import android.content.Context
import android.util.Log
import com.example.cryptotracker.data.api.CoinApi
import com.example.cryptotracker.data.model.toDomainModel
import com.example.cryptotracker.data.util.NetworkUtils
import com.example.cryptotracker.data.util.PreferencesManager
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of [CryptoRepository] that fetches data from CoinAPI
 * with caching support using SharedPreferences
 */
class CoinApiRepositoryImpl(
    private val coinApi: CoinApi,
    private val context: Context
) : CryptoRepository {
    
    private val preferencesManager = PreferencesManager(context)
    
    /**
     * Get real-time cryptocurrency prices from CoinAPI
     * Falls back to cached data if API call fails or device is offline
     * 
     * @return Result containing list of cryptocurrency data
     */
    override suspend fun getCryptoPrices(): Result<List<CryptoCurrency>> = withContext(Dispatchers.IO) {
        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.i("CoinApiRepository", "Device is offline, using cached data")
            return@withContext getCachedData()
        }
        
        Log.i("CoinApiRepository", "Fetching cryptocurrency prices from CoinAPI")
        try {
            val response = coinApi.getAssets()
            // Filter to get only the top cryptocurrencies with price data
            val filteredResponse = response
                .filter { it.isCrypto == 1 && it.priceUsd != null }
                .sortedByDescending { it.volume1dayUsd ?: 0.0 }
                .take(10)
            
            Log.i("CoinApiRepository", "Fetched ${filteredResponse.size} cryptocurrencies")
            
            // Convert to domain model
            val cryptoList = filteredResponse.toDomainModel()
            
            // Cache the result
            preferencesManager.saveCryptoList(cryptoList)
            Log.i("CoinApiRepository", "Cached ${cryptoList.size} cryptocurrencies")
            
            Result.Success(cryptoList)
        } catch (e: Exception) {
            Log.e("CoinApiRepository", "Error fetching cryptocurrencies: ${e.message}")
            
            // Return cached data if available
            return@withContext getCachedData(e)
        }
    }
    
    /**
     * Get cryptocurrency by id
     * Falls back to cached data if API call fails or device is offline
     * 
     * @param id Cryptocurrency id
     * @return Result containing cryptocurrency data
     */
    override suspend fun getCryptoById(id: String): Result<CryptoCurrency?> = withContext(Dispatchers.IO) {
        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.i("CoinApiRepository", "Device is offline, using cached data for id: $id")
            return@withContext getCachedCryptoById(id)
        }
        
        try {
            val cryptoList = coinApi.getAssets()
            val crypto = cryptoList.find { it.assetId.equals(id, ignoreCase = true) }?.toDomainModel()
            Result.Success(crypto)
        } catch (e: Exception) {
            Log.e("CoinApiRepository", "Error fetching cryptocurrency with id: $id - ${e.message}")
            
            // Return cached data if available
            return@withContext getCachedCryptoById(id, e)
        }
    }
    
    /**
     * Get cached cryptocurrency data
     * 
     * @param error Optional exception to include in error result if no cache is available
     * @return Result containing cached cryptocurrency data or error
     */
    private fun getCachedData(error: Exception? = null): Result<List<CryptoCurrency>> {
        val cachedData = preferencesManager.getCachedCryptoList()
        
        return if (cachedData != null) {
            val timestamp = preferencesManager.getLastUpdatedTimestamp()
            val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
            
            Log.i("CoinApiRepository", "Using cached data from $formattedTime with ${cachedData.size} cryptocurrencies")
            Result.Success(cachedData)
        } else {
            val errorMessage = "Failed to fetch cryptocurrency prices and no cached data available"
            Log.e("CoinApiRepository", errorMessage)
            Result.Error(errorMessage, error)
        }
    }
    
    /**
     * Get cached cryptocurrency by id
     * 
     * @param id Cryptocurrency id
     * @param error Optional exception to include in error result if no cache is available
     * @return Result containing cached cryptocurrency data or error
     */
    private fun getCachedCryptoById(id: String, error: Exception? = null): Result<CryptoCurrency?> {
        val cachedData = preferencesManager.getCachedCryptoList()
        
        return if (cachedData != null) {
            val crypto = cachedData.find { it.id.equals(id, ignoreCase = true) }
            Result.Success(crypto)
        } else {
            val errorMessage = "Failed to fetch cryptocurrency with id: $id and no cached data available"
            Result.Error(errorMessage, error)
        }
    }
}
