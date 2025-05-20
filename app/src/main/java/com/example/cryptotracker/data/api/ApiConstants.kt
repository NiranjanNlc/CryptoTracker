package com.example.cryptotracker.data.api

import com.example.cryptotracker.BuildConfig

/**
 * Constants for API configuration
 */
object ApiConstants {
    /**
     * CoinAPI API key
     * Retrieved from local.properties via BuildConfig
     */
    const val COIN_API_KEY = BuildConfig.COIN_API_KEY
    
    /**
     * CoinAPI base URL
     */
    const val COIN_API_BASE_URL = "https://rest.coinapi.io/v1/"
}
