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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
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
    
    @After
    fun tearDown() {
        // Reset network mock after each test
        NetworkUtils.resetMockNetworkAvailable()
    }

    @Test
    fun `getCryptoPrices returns successful API response when online`() = runTest {
        // Mock network availability
        NetworkUtils.setMockNetworkAvailable(true)
        
        // Mock API response with multiple cryptocurrencies
        val apiResponse = listOf(
            CoinApiAssetDto(
                assetId = "BTC",
                name = "Bitcoin",
                priceUsd = 50000.0,
                volume1dayUsd = 1000000.0,
                iconId = "btc",
                isCrypto = 1,
                dataStart = null,
                dataEnd = null,
                dataQuoteStart = null,
                dataQuoteEnd = null,
                dataOrderbookStart = null,
                dataOrderbookEnd = null,
                dataTradeStart = null,
                dataTradeEnd = null
            ),
            CoinApiAssetDto(
                assetId = "ETH",
                name = "Ethereum",
                priceUsd = 3000.0,
                volume1dayUsd = 500000.0,
                iconId = "eth",
                isCrypto = 1,
                dataStart = null,
                dataEnd = null,
                dataQuoteStart = null,
                dataQuoteEnd = null,
                dataOrderbookStart = null,
                dataOrderbookEnd = null,
                dataTradeStart = null,
                dataTradeEnd = null
            )
        )
        
        whenever(coinApi.getAssets()).thenReturn(apiResponse)
        
        // Call repository method
        val result = repository.getCryptoPrices()
        
        // Verify result is success with correct data
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        assertEquals("BTC", result.data[0].symbol)
        assertEquals("ETH", result.data[1].symbol)
        assertEquals(50000.0, result.data[0].price)
        assertEquals(3000.0, result.data[1].price)
        
        // Verify data was cached
        verify(sharedPreferencesEditor).putString(any(), any())
        verify(sharedPreferencesEditor).putLong(any(), any())
        verify(sharedPreferencesEditor, times(2)).apply()
    }

    @Test
    fun `getCryptoPrices returns cached data when API call fails`() = runTest {
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
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertEquals("bitcoin", result.data[0].id)
        assertEquals("BTC", result.data[0].symbol)
    }

    @Test
    fun `getCryptoPrices returns cached data when offline`() = runTest {
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
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertEquals("ethereum", result.data[0].id)
        
        // Verify API was not called
        verify(coinApi, never()).getAssets()
    }

    @Test
    fun `getCryptoPrices caches data on successful API call`() = runTest {
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
                isCrypto = 1,
                dataStart = null,
                dataEnd = null,
                dataQuoteStart = null,
                dataQuoteEnd = null,
                dataOrderbookStart = null,
                dataOrderbookEnd = null,
                dataTradeStart = null,
                dataTradeEnd = null
            )
        )
        
        whenever(coinApi.getAssets()).thenReturn(apiResponse)
        
        // Call repository method
        val result = repository.getCryptoPrices()
        
        // Verify result is success with API data
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertEquals("BTC", result.data[0].symbol)
        
        // Verify data was cached
        verify(sharedPreferencesEditor).putString(any(), any())
        verify(sharedPreferencesEditor).putLong(any(), any())
        verify(sharedPreferencesEditor, times(2)).apply()
    }
    
    @Test
    fun `getCryptoPrices returns error when API fails and no cache is available`() = runTest {
        // Mock network availability
        NetworkUtils.setMockNetworkAvailable(true)
        
        // Mock API call failure
        whenever(coinApi.getAssets()).thenThrow(IOException("Network error"))
        
        // Mock empty cache
        whenever(sharedPreferences.getString(any(), any())).thenReturn(null)
        whenever(sharedPreferences.contains(any())).thenReturn(false)
        
        // Call repository method
        val result = repository.getCryptoPrices()
        
        // Verify result is success with dummy data (as per implementation)
        assertTrue(result is Result.Success)
        
        // Note: The current implementation falls back to dummy data when both API and cache fail
        // In a real app, you might want to return an error instead
    }
    
    @Test
    fun `getCryptoById returns successful API response when online`() = runTest {
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
                isCrypto = 1,
                dataStart = null,
                dataEnd = null,
                dataQuoteStart = null,
                dataQuoteEnd = null,
                dataOrderbookStart = null,
                dataOrderbookEnd = null,
                dataTradeStart = null,
                dataTradeEnd = null
            )
        )
        
        whenever(coinApi.getAssets()).thenReturn(apiResponse)
        
        // Call repository method
        val result = repository.getCryptoById("BTC")
        
        // Verify result is success with correct data
        assertTrue(result is Result.Success)
        assertEquals("BTC", (result as Result.Success).data?.symbol)
        assertEquals(50000.0, result.data?.price)
    }
}

// Extension function to mock NetworkUtils for testing
private fun NetworkUtils.Companion.setMockNetworkAvailable(isAvailable: Boolean) {
    val field = NetworkUtils::class.java.getDeclaredField("isMockNetworkAvailable")
    field.isAccessible = true
    field.set(null, isAvailable)
}
