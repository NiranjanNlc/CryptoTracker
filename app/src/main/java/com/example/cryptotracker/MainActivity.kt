package com.example.cryptotracker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cryptotracker.ui.CryptoTrackerApp
import com.example.cryptotracker.ui.theme.CryptoTrackerTheme
import com.example.cryptotracker.util.PermissionUtil

class MainActivity : ComponentActivity() {
    
    private val TAG = "MainActivity"
    
    // Permission launcher for notification permission
    private val notificationPermissionLauncher = PermissionUtil.registerForNotificationPermission(this) { isGranted ->
        if (isGranted) {
            Log.i(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
            Toast.makeText(
                this,
                "Notification permission denied. You won't receive price alerts.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission if needed
        PermissionUtil.requestNotificationPermissionIfNeeded(this, notificationPermissionLauncher)
        
        setContent {
            CryptoTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CryptoTrackerApp()
                }
            }
        }
    }
}
