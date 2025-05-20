package com.example.cryptotracker.model

/**
 * Data class representing cryptocurrency information
 */
data class CryptoCurrency(
    val id: String,
    val name: String,
    val symbol: String,
    val price: Double,
    val priceChangePercentage24h: Double,
    val imageUrl: String
)

/**
 * Mock data provider for cryptocurrency information
 */
object CryptoDataProvider {
    fun getMockCryptoList(): List<CryptoCurrency> {
        return listOf(
            CryptoCurrency(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                price = 50345.67,
                priceChangePercentage24h = 2.34,
                imageUrl = "https://example.com/bitcoin.png"
            ),
            CryptoCurrency(
                id = "ethereum",
                name = "Ethereum",
                symbol = "ETH",
                price = 2456.78,
                priceChangePercentage24h = -1.23,
                imageUrl = "https://example.com/ethereum.png"
            ),
            CryptoCurrency(
                id = "cardano",
                name = "Cardano",
                symbol = "ADA",
                price = 1.45,
                priceChangePercentage24h = 5.67,
                imageUrl = "https://example.com/cardano.png"
            ),
            CryptoCurrency(
                id = "solana",
                name = "Solana",
                symbol = "SOL",
                price = 123.45,
                priceChangePercentage24h = 8.91,
                imageUrl = "https://example.com/solana.png"
            ),
            CryptoCurrency(
                id = "binancecoin",
                name = "Binance Coin",
                symbol = "BNB",
                price = 345.67,
                priceChangePercentage24h = -0.45,
                imageUrl = "https://example.com/binance.png"
            ),
            CryptoCurrency(
                id = "ripple",
                name = "XRP",
                symbol = "XRP",
                price = 0.78,
                priceChangePercentage24h = 3.21,
                imageUrl = "https://example.com/xrp.png"
            ),
            CryptoCurrency(
                id = "polkadot",
                name = "Polkadot",
                symbol = "DOT",
                price = 23.45,
                priceChangePercentage24h = -2.34,
                imageUrl = "https://example.com/polkadot.png"
            ),
            CryptoCurrency(
                id = "dogecoin",
                name = "Dogecoin",
                symbol = "DOGE",
                price = 0.12,
                priceChangePercentage24h = 12.34,
                imageUrl = "https://example.com/dogecoin.png"
            ),
            CryptoCurrency(
                id = "avalanche",
                name = "Avalanche",
                symbol = "AVAX",
                price = 78.90,
                priceChangePercentage24h = 4.56,
                imageUrl = "https://example.com/avalanche.png"
            ),
            CryptoCurrency(
                id = "chainlink",
                name = "Chainlink",
                symbol = "LINK",
                price = 15.67,
                priceChangePercentage24h = -3.45,
                imageUrl = "https://example.com/chainlink.png"
            )
        )
    }
}
