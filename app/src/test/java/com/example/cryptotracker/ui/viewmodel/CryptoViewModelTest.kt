package com.example.cryptotracker.ui.viewmodel

import com.example.cryptotracker.data.repository.CryptoRepository
import com.example.cryptotracker.data.util.Result
import com.example.cryptotracker.model.CryptoCurrency
import com.example.cryptotracker.ui.viewmodel.CryptoListState
import com.example.cryptotracker.ui.viewmodel.CryptoViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class CryptoViewModelTest {

    private lateinit var repository: CryptoRepository
    private lateinit var viewModel: CryptoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCryptoPrices sets state to Success when repository returns successful result`() = runTest {
        // Arrange
        val cryptoList = listOf(
            CryptoCurrency(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                price = 50000.0,
                priceChangePercentage24h = 2.5,
                imageUrl = "https://example.com/btc.png"
            )
        )
        whenever(repository.getCryptoPrices()).thenReturn(Result.Success(cryptoList))
        
        // Act
        viewModel = CryptoViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle() // Advance coroutines
        
        // Assert
        val state = viewModel.cryptoListState.first()
        assertTrue(state is CryptoListState.Success)
        assertEquals(cryptoList, (state as CryptoListState.Success).data)
    }

    @Test
    fun `loadCryptoPrices sets state to Error when repository returns error result`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        whenever(repository.getCryptoPrices()).thenReturn(Result.Error(errorMessage))
        
        // Act
        viewModel = CryptoViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle() // Advance coroutines
        
        // Assert
        val state = viewModel.cryptoListState.first()
        assertTrue(state is CryptoListState.Error)
        assertEquals(errorMessage, (state as CryptoListState.Error).message)
    }

    @Test
    fun `loadCryptoPrices sets state to Error with specific message for IOException`() = runTest {
        // Arrange
        whenever(repository.getCryptoPrices()).thenReturn(
            Result.Error("Network error", IOException("Connection failed"))
        )
        
        // Act
        viewModel = CryptoViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle() // Advance coroutines
        
        // Assert
        val state = viewModel.cryptoListState.first()
        assertTrue(state is CryptoListState.Error)
        assertEquals(
            "Network error. Please check your connection and try again.", 
            (state as CryptoListState.Error).message
        )
    }

    @Test
    fun `loadCryptoPrices sets state to Error when repository returns empty list`() = runTest {
        // Arrange
        whenever(repository.getCryptoPrices()).thenReturn(Result.Success(emptyList()))
        
        // Act
        viewModel = CryptoViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle() // Advance coroutines
        
        // Assert
        val state = viewModel.cryptoListState.first()
        assertTrue(state is CryptoListState.Error)
        assertEquals(
            "No cryptocurrency data available", 
            (state as CryptoListState.Error).message
        )
    }

    @Test
    fun `loadCryptoPrices prevents multiple simultaneous loads`() = runTest {
        // Arrange - Set up a delayed response from the repository
        var callCount = 0
        whenever(repository.getCryptoPrices()).thenAnswer {
            callCount++
            Result.Success(listOf(
                CryptoCurrency(
                    id = "bitcoin",
                    name = "Bitcoin",
                    symbol = "BTC",
                    price = 50000.0,
                    priceChangePercentage24h = 2.5,
                    imageUrl = "https://example.com/btc.png"
                )
            ))
        }
        
        // Act
        viewModel = CryptoViewModel(repository)
        
        // Call loadCryptoPrices multiple times in quick succession
        viewModel.loadCryptoPrices()
        viewModel.loadCryptoPrices()
        viewModel.loadCryptoPrices()
        
        testDispatcher.scheduler.advanceUntilIdle() // Advance coroutines
        
        // Assert - Repository should only be called twice (once in init and once from our explicit call)
        assertEquals(2, callCount)
    }
}
