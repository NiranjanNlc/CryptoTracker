package com.example.cryptotracker.model

/**
 * Data class representing a cryptocurrency price alert
 */
data class Alert(
    val id: String,
    val cryptoSymbol: String,
    val cryptoName: String,
    val threshold: Double,
    val isUpperBound: Boolean, // true if alert triggers when price goes above threshold
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
