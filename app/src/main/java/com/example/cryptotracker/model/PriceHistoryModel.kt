package com.example.cryptotracker.model

import java.util.Date
import kotlin.random.Random

/**
 * Data class representing a price data point for charts
 */
data class PriceDataPoint(
    val timestamp: Date,
    val price: Float
)

/**
 * Data class representing price history for a cryptocurrency
 */
data class PriceHistory(
    val cryptoId: String,
    val dataPoints: List<PriceDataPoint>
)

/**
 * Data class representing a price point with timestamp as Long and price as Double
 */
data class PricePoint(
    val time: Long,
    val price: Double
)

/**
 * Utility class to generate mock price history data
 */
object PriceHistoryGenerator {
    /**
     * Generates mock price history data for a cryptocurrency
     * @param cryptoId The ID of the cryptocurrency
     * @param basePrice The base price to generate variations around
     * @param hours The number of hours of history to generate (default 24 hours)
     * @return A PriceHistory object with mock data
     */
    fun generateMockPriceHistory(
        cryptoId: String,
        basePrice: Double,
        hours: Int = 24
    ): PriceHistory {
        val now = Date()
        val dataPoints = mutableListOf<PriceDataPoint>()
        
        // Generate data points for the specified number of hours
        for (i in hours downTo 0) {
            val timestamp = Date(now.time - (i * 60 * 60 * 1000))
            
            // Generate a price with some random variation
            val variation = Random.nextDouble(-0.05, 0.05) // +/- 5% variation
            val price = basePrice * (1 + variation)
            
            dataPoints.add(PriceDataPoint(timestamp, price.toFloat()))
        }
        
        return PriceHistory(cryptoId, dataPoints)
    }

    /**
     * Generates 24-hour mock price history data for a cryptocurrency
     * @param cryptoSymbol The symbol of the cryptocurrency (e.g., "BTC")
     * @return A list of PricePoint objects representing hourly price points for the last 24 hours
     */
    fun getMockPriceHistory(cryptoSymbol: String): List<PricePoint> {
        // Get the current time in milliseconds
        val currentTime = System.currentTimeMillis()
        
        // Find the base price for the given crypto symbol from the existing data
        val cryptoCurrency = CryptoDataProvider.getMockCryptoList()
            .find { it.symbol == cryptoSymbol }
        
        // Use the found price or a default value if not found
        val basePrice = cryptoCurrency?.price ?: 1000.0
        
        val pricePoints = mutableListOf<PricePoint>()
        
        // Generate 24 hourly price points
        for (hourAgo in 24 downTo 0) {
            // Calculate timestamp for this hour
            val timestamp = currentTime - (hourAgo * 60 * 60 * 1000)
            
            // Generate a random price fluctuation (±5%)
            val fluctuation = 1.0 + Random.nextDouble(-0.05, 0.05)
            
            // Calculate the price for this hour
            val hourPrice = if (hourAgo == 0) {
                // Current hour uses the exact base price
                basePrice
            } else {
                // Previous hours use the base price with random fluctuation
                basePrice * fluctuation
            }
            
            pricePoints.add(PricePoint(timestamp, hourPrice))
        }
        
        return pricePoints
    }
}
