package com.example.cryptotracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptotracker.data.api.CoinApi
import com.example.cryptotracker.data.model.CoinApiAssetDto
import com.example.cryptotracker.data.util.NetworkUtils
import com.example.cryptotracker.data.util.PreferencesManager
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

class CoinApiRepositoryImplTest {

    private lateinit var coinApi: CoinApi
    private lateinit var context: Context
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var repository: CoinApiRepositoryImpl
    private lateinit var gson: Gson

    @Before
    fun setup() {
        coinApi = mock()
        context = mock()
        sharedPreferences = mock()
        sharedPreferencesEditor = mock()
        gson = Gson()
        
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putString(any(), any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putLong(any(), any())).thenReturn(sharedPreferencesEditor)
        
        preferencesManager = PreferencesManager(context)
        
        // Use reflection to replace the real preferencesManager with our mock
        val field = PreferencesManager::class.java.getDeclaredField("sharedPreferences")
        field.isAccessible = true
        field.set(preferencesManager, sharedPreferences)
        
        repository = CoinApiRepositoryImpl(coinApi, context)
        
        // Use reflection to replace the real preferencesManager with our mock
        val repoField = CoinApiRepositoryImpl::class.java.getDeclaredField("preferencesManager")
        repoField.isAccessible = true
        repoField.set(repository, preferencesManager)
    }

    @Test
    fun `getCryptoPrices returns cached data when API call fails`() = runBlocking {
        // Mock network availability
        NetworkUtils.setMockNetworkAvailable(true)
        
        // Mock API call failure
        whenever(coinApi.getAssets()).thenThrow(IOException("Network error"))
        
        // Mock cached data
        val cachedCryptos = listOf(
            CryptoCurrency(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                price = 50000.0,
                priceChangePercentage24h = 2.5,
                imageUrl = "https://example.com/btc.png"
            )
        )
        val cachedJson = gson.toJson(cachedCryptos)
        
        whenever(sharedPreferences.getString(any(), any())).thenReturn(cachedJson)
        whenever(sharedPreferences.getLong(any(), any())).thenReturn(System.currentTimeMillis())
        whenever(sharedPreferences.contains(any())).thenReturn(true)
        
        // Call repository method
        val result = repository.getCryptoPrices()
        
        // Verify result is success with cached data
        assert(result is Result.Success)
        assert((result as Result.Success).data.size == 1)
        assert(result.data[0].id == "bitcoin")
    }

    @Test
    fun `getCryptoPrices returns cached data when offline`() = runBlocking {
        // Mock network unavailability
        NetworkUtils.setMockNetworkAvailable(false)
        
        // Mock cached data
        val cachedCryptos = listOf(
            CryptoCurrency(
                id = "ethereum",
                name = "Ethereum",
                symbol = "ETH",
                price = 3000.0,
                priceChangePercentage24h = -1.5,
                imageUrl = "https://example.com/eth.png"
            )
        )
        val cachedJson = gson.toJson(cachedCryptos)
        
        whenever(sharedPreferences.getString(any(), any())).thenReturn(cachedJson)
        whenever(sharedPreferences.getLong(any(), any())).thenReturn(System.currentTimeMillis())
        whenever(sharedPreferences.contains(any())).thenReturn(true)
        
        // Call repository method
        val result = repository.getCryptoPrices()
        
        // Verify result is success with cached data
        assert(result is Result.Success)
        assert((result as Result.Success).data.size == 1)
        assert(result.data[0].id == "ethereum")
        
        // Verify API was not called
        verify(coinApi, org.mockito.kotlin.never()).getAssets()
    }

    @Test
    fun `getCryptoPrices caches data on successful API call`() = runBlocking {
        // Mock network availability
        NetworkUtils.setMockNetworkAvailable(true)
        
        // Mock API response
        val apiResponse = listOf(
            CoinApiAssetDto(
                assetId = "BTC",
                name = "Bitcoin",
                priceUsd = 50000.0,
                volume1dayUsd = 1000000.0,
                iconId = "btc",
                dataStart = null,
                dataEnd = null,
                dataQuoteStart = null,
                dataQuoteEnd = null,
                dataOrderbookStart = null,
                dataOrderbookEnd = null,
                dataTradeStart = null,
                dataTradeEnd = null,
                isCrypto = 1
            )
        )
        
        whenever(coinApi.getAssets()).thenReturn(apiResponse)
        
        // Call repository method
        val result = repository.getCryptoPrices()
        
        // Verify result is success with API data
        assert(result is Result.Success)
        assert((result as Result.Success).data.size == 1)
        assert(result.data[0].symbol == "BTC")
        
        // Verify data was cached
        verify(sharedPreferencesEditor).putString(any(), any())
        verify(sharedPreferencesEditor).putLong(any(), any())
        verify(sharedPreferencesEditor, org.mockito.kotlin.times(2)).apply()
    }
}

// Extension function to mock NetworkUtils for testing
private fun NetworkUtils.Companion.setMockNetworkAvailable(isAvailable: Boolean) {
    val field = NetworkUtils::class.java.getDeclaredField("isMockNetworkAvailable")
    field.isAccessible = true
    field.set(null, isAvailable)
}
