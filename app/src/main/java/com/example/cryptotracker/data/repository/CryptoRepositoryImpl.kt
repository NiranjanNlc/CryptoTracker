package com.example.cryptotracker.data.repository

import com.example.cryptotracker.data.api.CoinGeckoApi
import com.example.cryptotracker.data.model.toDomainModel
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of [CryptoRepository] that fetches data from CoinGecko API
 */
class CryptoRepositoryImpl(
    private val coinGeckoApi: CoinGeckoApi
) : CryptoRepository {
    
    /**
     * Get real-time cryptocurrency prices from CoinGecko API
     * 
     * @return Result containing list of cryptocurrency data
     */
    override suspend fun getCryptoPrices(): Result<List<CryptoCurrency>> = withContext(Dispatchers.IO) {
        try {
            val response = coinGeckoApi.getCryptoMarkets()
            Result.Success(response.toDomainModel())
        } catch (e: Exception) {
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
            val cryptoList = coinGeckoApi.getCryptoMarkets()
            val crypto = cryptoList.find { it.id == id }?.toDomainModel()
            Result.Success(crypto)
        } catch (e: Exception) {
            Result.Error("Failed to fetch cryptocurrency with id: $id", e)
        }
    }
}
