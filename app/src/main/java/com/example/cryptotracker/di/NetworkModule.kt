package com.example.cryptotracker.di

import com.example.cryptotracker.data.api.ApiConstants
import com.example.cryptotracker.data.api.CoinApi
import com.example.cryptotracker.data.api.CoinGeckoApi
import com.example.cryptotracker.data.repository.CoinApiRepositoryImpl
import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.repository.CryptoRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Object that provides network-related dependencies
 */
object NetworkModule {
    
    // Base URL for CoinGecko API
    private const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"
    
    /**
     * Provides Moshi instance for JSON parsing
     */
    private fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    /**
     * Provides OkHttpClient with logging interceptor
     */
    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Provides Retrofit instance for CoinGecko
     */
    private fun provideCoinGeckoRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(COINGECKO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    /**
     * Provides Retrofit instance for CoinAPI
     */
    private fun provideCoinApiRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.COIN_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    /**
     * Provides CoinGeckoApi implementation
     */
    fun provideCoinGeckoApi(): CoinGeckoApi {
        val moshi = provideMoshi()
        val okHttpClient = provideOkHttpClient()
        val retrofit = provideCoinGeckoRetrofit(okHttpClient, moshi)
        return retrofit.create(CoinGeckoApi::class.java)
    }
    
    /**
     * Provides CoinApi implementation
     */
    fun provideCoinApi(): CoinApi {
        val moshi = provideMoshi()
        val okHttpClient = provideOkHttpClient()
        val retrofit = provideCoinApiRetrofit(okHttpClient, moshi)
        return retrofit.create(CoinApi::class.java)
    }
    
    /**
     * Provides CryptoRepository implementation
     * Currently using CoinAPI implementation
     */
    fun provideCryptoRepository(): CryptoRepository {
        // Use CoinAPI implementation instead of CoinGecko
        return CoinApiRepositoryImpl(provideCoinApi())
        
        // To switch back to CoinGecko, uncomment the line below and comment the line above
        // return CryptoRepositoryImpl(provideCoinGeckoApi())
    }
}
