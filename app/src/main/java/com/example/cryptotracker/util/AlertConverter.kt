package com.example.cryptotracker.util

import com.example.cryptotracker.model.Alert
import com.example.cryptotracker.model.CryptoAlert

/**
 * Utility class for converting between Alert and CryptoAlert models
 */
object AlertConverter {

    /**
     * Convert from CryptoAlert to Alert
     *
     * @param cryptoAlert The CryptoAlert to convert
     * @return The converted Alert
     */
    fun fromCryptoAlert(cryptoAlert: CryptoAlert): Alert {
        return Alert(
            id = cryptoAlert.id,
            cryptoSymbol = cryptoAlert.cryptoSymbol,
            cryptoName = cryptoAlert.cryptoName,
            threshold = cryptoAlert.targetPrice,
            isUpperBound = cryptoAlert.isAboveTarget,
            isEnabled = cryptoAlert.isEnabled,
            createdAt = cryptoAlert.createdAt
        )
    }

    /**
     * Convert from Alert to CryptoAlert
     *
     * @param alert The Alert to convert
     * @return The converted CryptoAlert
     */
    fun toCryptoAlert(alert: Alert): CryptoAlert {
        return CryptoAlert(
            id = alert.id,
            cryptoSymbol = alert.cryptoSymbol,
            cryptoName = alert.cryptoName,
            targetPrice = alert.threshold,
            isAboveTarget = alert.isUpperBound,
            isEnabled = alert.isEnabled,
            createdAt = alert.createdAt
        )
    }

    /**
     * Convert a list of CryptoAlerts to a list of Alerts
     *
     * @param cryptoAlerts The list of CryptoAlerts to convert
     * @return The converted list of Alerts
     */
    fun fromCryptoAlertList(cryptoAlerts: List<CryptoAlert>): List<Alert> {
        return cryptoAlerts.map { fromCryptoAlert(it) }
    }

    /**
     * Convert a list of Alerts to a list of CryptoAlerts
     *
     * @param alerts The list of Alerts to convert
     * @return The converted list of CryptoAlerts
     */
    fun toCryptoAlertList(alerts: List<Alert>): List<CryptoAlert> {
        return alerts.map { toCryptoAlert(it) }
    }
}
