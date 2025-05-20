package com.example.cryptotracker.data.model

import com.example.cryptotracker.model.CryptoCurrency
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoDtoTest {

    @Test
    fun `toDomainModel correctly maps single DTO to domain model`() {
        // Arrange
        val cryptoDto = CryptoDto(
            id = "bitcoin",
            name = "Bitcoin",
            symbol = "btc",
            currentPrice = 50000.0,
            priceChangePercentage24h = 2.5,
            imageUrl = "https://example.com/bitcoin.png"
        )
        
        // Act
        val domainModel = cryptoDto.toDomainModel()
        
        // Assert
        assertEquals("bitcoin", domainModel.id)
        assertEquals("Bitcoin", domainModel.name)
        assertEquals("BTC", domainModel.symbol) // Verify symbol is uppercase
        assertEquals(50000.0, domainModel.price, 0.001)
        assertEquals(2.5, domainModel.priceChangePercentage24h, 0.001)
        assertEquals("https://example.com/bitcoin.png", domainModel.imageUrl)
    }
    
    @Test
    fun `toDomainModel correctly maps list of DTOs to domain models`() {
        // Arrange
        val cryptoDtoList = listOf(
            CryptoDto(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "btc",
                currentPrice = 50000.0,
                priceChangePercentage24h = 2.5,
                imageUrl = "https://example.com/bitcoin.png"
            ),
            CryptoDto(
                id = "ethereum",
                name = "Ethereum",
                symbol = "eth",
                currentPrice = 3000.0,
                priceChangePercentage24h = 1.5,
                imageUrl = "https://example.com/ethereum.png"
            )
        )
        
        // Act
        val domainModelList = cryptoDtoList.toDomainModel()
        
        // Assert
        assertEquals(2, domainModelList.size)
        
        // First item
        assertEquals("bitcoin", domainModelList[0].id)
        assertEquals("Bitcoin", domainModelList[0].name)
        assertEquals("BTC", domainModelList[0].symbol)
        assertEquals(50000.0, domainModelList[0].price, 0.001)
        assertEquals(2.5, domainModelList[0].priceChangePercentage24h, 0.001)
        assertEquals("https://example.com/bitcoin.png", domainModelList[0].imageUrl)
        
        // Second item
        assertEquals("ethereum", domainModelList[1].id)
        assertEquals("Ethereum", domainModelList[1].name)
        assertEquals("ETH", domainModelList[1].symbol)
        assertEquals(3000.0, domainModelList[1].price, 0.001)
        assertEquals(1.5, domainModelList[1].priceChangePercentage24h, 0.001)
        assertEquals("https://example.com/ethereum.png", domainModelList[1].imageUrl)
    }
}
