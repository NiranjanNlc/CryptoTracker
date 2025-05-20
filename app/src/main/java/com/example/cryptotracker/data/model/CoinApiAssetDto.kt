package com.example.cryptotracker.data.model

import com.example.cryptotracker.model.CryptoCurrency
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for cryptocurrency asset data from CoinAPI
 */
@JsonClass(generateAdapter = true)
data class CoinApiAssetDto(
    @Json(name = "asset_id") val assetId: String,
    @Json(name = "name") val name: String,
    @Json(name = "price_usd") val priceUsd: Double?,
    @Json(name = "volume_1day_usd") val volume1dayUsd: Double?,
    @Json(name = "id_icon") val iconId: String?,
    @Json(name = "data_start") val dataStart: String?,
    @Json(name = "data_end") val dataEnd: String?,
    @Json(name = "data_quote_start") val dataQuoteStart: String?,
    @Json(name = "data_quote_end") val dataQuoteEnd: String?,
    @Json(name = "data_orderbook_start") val dataOrderbookStart: String?,
    @Json(name = "data_orderbook_end") val dataOrderbookEnd: String?,
    @Json(name = "data_trade_start") val dataTradeStart: String?,
    @Json(name = "data_trade_end") val dataTradeEnd: String?,
    @Json(name = "type_is_crypto") val isCrypto: Int?
)

/**
 * Extension function to convert CoinAPI DTO to domain model
 */
fun CoinApiAssetDto.toDomainModel(): CryptoCurrency? {
    // Only convert crypto assets with price data
    if (isCrypto != 1 || priceUsd == null) {
        return null
    }
    
    return CryptoCurrency(
        id = assetId.lowercase(),
        name = name,
        symbol = assetId,
        price = priceUsd,
        priceChangePercentage24h = 0.0, // CoinAPI doesn't provide this in the assets endpoint
        imageUrl = iconId?.let { "https://s3.eu-central-1.amazonaws.com/bbxt-static-icons/type-id/png_512/$it.png" } ?: ""
    )
}

/**
 * Extension function to convert list of CoinAPI DTOs to list of domain models
 */
fun List<CoinApiAssetDto>.toDomainModel(): List<CryptoCurrency> {
    return mapNotNull { it.toDomainModel() }
}
