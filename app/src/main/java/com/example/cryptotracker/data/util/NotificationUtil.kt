package com.example.cryptotracker.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cryptotracker.MainActivity
import com.example.cryptotracker.R
import com.example.cryptotracker.model.Alert

/**
 * Utility class for handling notifications in the app
 */
object NotificationUtil {
    private const val CHANNEL_ID = "crypto_price_alerts"
    private const val CHANNEL_NAME = "Price Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for cryptocurrency price alerts"
    private const val NOTIFICATION_GROUP = "com.example.cryptotracker.PRICE_ALERTS"
    
    /**
     * Create the notification channel for API 26+
     * This must be called when the application starts
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show a notification for a triggered price alert
     * 
     * @param context Application context
     * @param alert The alert that was triggered
     * @param currentPrice The current price that triggered the alert
     * @return true if notification was shown successfully
     */
    fun showAlertNotification(context: Context, alert: Alert, currentPrice: Double): Boolean {
        try {
            // Create an intent to open the app when the notification is tapped
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("ALERT_ID", alert.id)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context, 
                alert.id.hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Build the notification content
            val title = "${alert.cryptoName} (${alert.cryptoSymbol}) Alert"
            val message = if (alert.isUpperBound) {
                "Price has risen above ${formatPrice(alert.threshold)}! Current price: ${formatPrice(currentPrice)}"
            } else {
                "Price has fallen below ${formatPrice(alert.threshold)}! Current price: ${formatPrice(currentPrice)}"
            }
            
            // Create the notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You'll need to create this icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(NOTIFICATION_GROUP)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            
            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(alert.id.hashCode(), builder.build())
            }
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Format a price value for display in notifications
     */
    private fun formatPrice(price: Double): String {
        return if (price < 1.0) {
            "$${String.format("%.4f", price)}"
        } else {
            "$${String.format("%.2f", price)}"
        }
    }
}
