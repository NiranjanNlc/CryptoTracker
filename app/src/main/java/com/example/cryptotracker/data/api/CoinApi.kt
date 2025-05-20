package com.example.cryptotracker.data.api

import com.example.cryptotracker.data.model.CoinApiAssetDto
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * Retrofit interface for CoinAPI
 */
interface CoinApi {
    /**
     * Get cryptocurrency assets data
     * 
     * @param apiKey API key for authentication
     * @return List of cryptocurrency assets data
     */
    @GET("assets")
    suspend fun getAssets(
        @Header("X-CoinAPI-Key") apiKey: String = ApiConstants.COIN_API_KEY
    ): List<CoinApiAssetDto>
}
