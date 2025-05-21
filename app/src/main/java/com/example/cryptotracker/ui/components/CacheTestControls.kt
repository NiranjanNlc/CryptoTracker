package com.example.cryptotracker.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cryptotracker.data.util.NetworkUtils
import com.example.cryptotracker.data.util.PreferencesManager
import com.example.cryptotracker.util.NetworkToggleHelper

/**
 * UI component for testing cache functionality
 * Provides buttons to check network status, toggle airplane mode, and clear cache
 */
@Composable
fun CacheTestControls(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cache Testing Controls",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NetworkStatusIndicator(context)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { NetworkToggleHelper.showNetworkToggleInstructions(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Toggle Network Connectivity")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    preferencesManager.clearCache()
                    onRefresh()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Cache")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CacheStatusInfo(preferencesManager)
        }
    }
}

/**
 * Displays the current network connectivity status
 */
@Composable
private fun NetworkStatusIndicator(context: Context) {
    val isOnline = NetworkUtils.isNetworkAvailable(context)
    val isAirplaneMode = NetworkToggleHelper.isAirplaneModeOn(context)
    
    val statusText = when {
        isAirplaneMode -> "Airplane Mode: ON (Offline)"
        isOnline -> "Network Status: ONLINE"
        else -> "Network Status: OFFLINE"
    }
    
    val statusColor = if (isOnline && !isAirplaneMode) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    
    Text(
        text = statusText,
        color = statusColor,
        style = MaterialTheme.typography.bodyLarge
    )
}

/**
 * Displays information about the current cache status
 */
@Composable
private fun CacheStatusInfo(preferencesManager: PreferencesManager) {
    val hasCachedData = preferencesManager.hasCachedData()
    val lastUpdated = if (hasCachedData) {
        val timestamp = preferencesManager.getLastUpdatedTimestamp()
        val formattedTime = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", 
            java.util.Locale.getDefault()
        ).format(java.util.Date(timestamp))
        "Last cached: $formattedTime"
    } else {
        "No cached data available"
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cache Status: ${if (hasCachedData) "Available" else "Empty"}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (hasCachedData) {
            Text(
                text = lastUpdated,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
