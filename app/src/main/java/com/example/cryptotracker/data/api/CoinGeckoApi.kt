package com.example.cryptotracker.data.api

import com.example.cryptotracker.data.model.CryptoDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for CoinGecko API
 */
interface CoinGeckoApi {
    /**
     * Get cryptocurrency market data
     * 
     * @param vsCurrency The target currency of market data (usd, eur, jpy, etc.)
     * @param perPage Number of results per page
     * @param page Page number
     * @param sparkline Include sparkline 7 days data
     * @param order Sort results by field
     * @param priceChangePercentage Include price change percentage in given time range
     * 
     * @return List of cryptocurrency market data
     */
    @GET("coins/markets")
    suspend fun getCryptoMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("order") order: String = "market_cap_desc",
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): List<CryptoDto>
}
