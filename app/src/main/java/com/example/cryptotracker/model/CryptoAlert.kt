package com.example.cryptotracker.model

/**
 * Data class representing a cryptocurrency price alert
 */
data class CryptoAlert(
    val id: String,
    val cryptoName: String,
    val cryptoSymbol: String,
    val targetPrice: Double,
    val isAboveTarget: Boolean, // true if alert is for price above target, false for below
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Provider for sample crypto alerts data
 */
object CryptoAlertProvider {
    val sampleAlerts = listOf(
        CryptoAlert(
            id = "1",
            cryptoName = "Bitcoin",
            cryptoSymbol = "BTC",
            targetPrice = 70000.0,
            isAboveTarget = true
        ),
        CryptoAlert(
            id = "2",
            cryptoName = "Ethereum",
            cryptoSymbol = "ETH",
            targetPrice = 4000.0,
            isAboveTarget = false
        ),
        CryptoAlert(
            id = "3",
            cryptoName = "Solana",
            cryptoSymbol = "SOL",
            targetPrice = 150.0,
            isAboveTarget = true
        ),
        CryptoAlert(
            id = "4",
            cryptoName = "Cardano",
            cryptoSymbol = "ADA",
            targetPrice = 1.0,
            isAboveTarget = false
        ),
        CryptoAlert(
            id = "5",
            cryptoName = "Dogecoin",
            cryptoSymbol = "DOGE",
            targetPrice = 0.25,
            isAboveTarget = true
        )
    )
}
