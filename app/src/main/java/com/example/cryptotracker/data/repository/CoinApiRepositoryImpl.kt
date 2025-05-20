package com.example.cryptotracker.data.repository

import android.util.Log
import com.example.cryptotracker.data.api.CoinApi
import com.example.cryptotracker.data.model.toDomainModel
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of [CryptoRepository] that fetches data from CoinAPI
 */
class CoinApiRepositoryImpl(
    private val coinApi: CoinApi
) : CryptoRepository {
    
    /**
     * Get real-time cryptocurrency prices from CoinAPI
     * 
     * @return Result containing list of cryptocurrency data
     */
    override suspend fun getCryptoPrices(): Result<List<CryptoCurrency>> = withContext(Dispatchers.IO) {
       Log.i("CoinApiRepository", "Fetching cryptocurrency prices from CoinAPI")
        try {
            val response = coinApi.getAssets()
            // Filter to get only the top cryptocurrencies with price data
            val filteredResponse = response
                .filter { it.isCrypto == 1 && it.priceUsd != null }
                .sortedByDescending { it.volume1dayUsd ?: 0.0 }
                .take(10)
            
            Log.i("CoinApiRepository", "Fetched ${filteredResponse.size} cryptocurrencies")
            Result.Success(filteredResponse.toDomainModel())
        } catch (e: Exception) {
            Log.e("CoinApiRepository", "Error fetching cryptocurrencies: ${e.message}")
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
            val cryptoList = coinApi.getAssets()
            val crypto = cryptoList.find { it.assetId.equals(id, ignoreCase = true) }?.toDomainModel()
            Result.Success(crypto)
        } catch (e: Exception) {
            Result.Error("Failed to fetch cryptocurrency with id: $id", e)
        }
    }
}
