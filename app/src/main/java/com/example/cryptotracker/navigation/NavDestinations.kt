package com.example.cryptotracker.navigation

/**
 * Sealed class representing the navigation destinations in the app
 */
sealed class NavDestinations(val route: String) {
    object Prices : NavDestinations("prices")
    object Alerts : NavDestinations("alerts")
    object AlertSetup : NavDestinations("alert_setup")
    object CryptoDetail : NavDestinations("crypto_detail/{cryptoId}") {
        fun createRoute(cryptoId: String): String = "crypto_detail/$cryptoId"
    }
}
