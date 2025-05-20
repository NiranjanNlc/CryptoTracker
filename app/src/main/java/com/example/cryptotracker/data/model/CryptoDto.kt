package com.example.cryptotracker.data.model

import com.example.cryptotracker.model.CryptoCurrency
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for cryptocurrency data from CoinGecko API
 */
@JsonClass(generateAdapter = true)
data class CryptoDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "current_price") val currentPrice: Double,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double,
    @Json(name = "image") val imageUrl: String
)

/**
 * Extension function to convert DTO to domain model
 */
fun CryptoDto.toDomainModel(): CryptoCurrency {
    return CryptoCurrency(
        id = id,
        name = name,
        symbol = symbol.uppercase(),
        price = currentPrice,
        priceChangePercentage24h = priceChangePercentage24h,
        imageUrl = imageUrl
    )
}

/**
 * Extension function to convert list of DTOs to list of domain models
 */
fun List<CryptoDto>.toDomainModel(): List<CryptoCurrency> {
    return map { it.toDomainModel() }
}
