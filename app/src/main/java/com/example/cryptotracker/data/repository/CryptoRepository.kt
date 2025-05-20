package com.example.cryptotracker.data.repository

import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency

/**
 * Repository interface for cryptocurrency data
 */
interface CryptoRepository {
    /**
     * Get real-time cryptocurrency prices
     * 
     * @return Result containing list of cryptocurrency data
     */
    suspend fun getCryptoPrices(): Result<List<CryptoCurrency>>
    
    /**
     * Get cryptocurrency by id
     * 
     * @param id Cryptocurrency id
     * @return Result containing cryptocurrency data
     */
    suspend fun getCryptoById(id: String): Result<CryptoCurrency?>
}
