package com.example.cryptotracker.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/**
 * Helper class to assist with testing network connectivity features
 */
object NetworkToggleHelper {

    /**
     * Shows a dialog with instructions on how to toggle airplane mode
     * to test offline functionality
     *
     * @param context The context to show the dialog in
     */
    fun showNetworkToggleInstructions(context: Context) {
        val message = """
            To test the offline caching functionality:
            
            1. First, load the cryptocurrency data while online
            2. Then toggle Airplane Mode on to disconnect from the network
            3. Refresh the app to see cached data being used
            4. Toggle Airplane Mode off to reconnect
            
            Would you like to go to Airplane Mode settings now?
        """.trimIndent()

        AlertDialog.Builder(context)
            .setTitle("Test Offline Mode")
            .setMessage(message)
            .setPositiveButton("Go to Settings") { _, _ ->
                openAirplaneModeSettings(context)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Opens the appropriate settings page for toggling airplane mode
     *
     * @param context The context to use for opening settings
     */
    private fun openAirplaneModeSettings(context: Context) {
        try {
            // For newer Android versions, open the Network & Internet settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = android.content.Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                context.startActivity(intent)
            } else {
                // For older versions, open the general settings
                val intent = android.content.Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open settings. Please toggle Airplane Mode manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Check if the device is currently in airplane mode
     *
     * @param context The context to use for checking airplane mode
     * @return True if airplane mode is enabled, false otherwise
     */
    fun isAirplaneModeOn(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
        } else {
            @Suppress("DEPRECATION")
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.AIRPLANE_MODE_ON, 0
            ) != 0
        }
    }
}
