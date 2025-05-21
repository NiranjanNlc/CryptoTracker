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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.util.PreferencesManager
import com.example.cryptotracker.data.worker.CryptoPriceWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UI component for testing and monitoring WorkManager functionality
 */
@Composable
fun WorkManagerTestControls(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    
    var lastUpdateTime by remember { mutableStateOf("Never") }
    var workState by remember { mutableStateOf("Unknown") }
    
    // Update the last update time from preferences
    LaunchedEffect(Unit) {
        updateLastUpdateTime(preferencesManager, onTimeUpdated = { lastUpdateTime = it })
        updateWorkState(context) { workState = it }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "WorkManager Status",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Last Update: $lastUpdateTime",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Work State: $workState",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Manually trigger a price update
                        val repository = CryptoTrackerApplication.getRepository()
                        repository.getCryptoPrices()
                        
                        // Update the UI with the new last update time
                        updateLastUpdateTime(preferencesManager, onTimeUpdated = { lastUpdateTime = it })
                    }
                }
            ) {
                Text(text = "Update Prices Now")
            }
        }
    }
}

/**
 * Update the last update time from preferences
 */
private fun updateLastUpdateTime(
    preferencesManager: PreferencesManager,
    onTimeUpdated: (String) -> Unit
) {
    val timestamp = preferencesManager.getLastUpdatedTimestamp()
    val timeString = if (timestamp > 0) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(timestamp))
    } else {
        "Never"
    }
    onTimeUpdated(timeString)
}

/**
 * Update the WorkManager state
 */
private fun updateWorkState(
    context: Context,
    onStateUpdated: (String) -> Unit
) {
    WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(CryptoPriceWorker.WORK_NAME)
        .observeForever { workInfos ->
            if (workInfos.isNullOrEmpty()) {
                onStateUpdated("Not scheduled")
            } else {
                val states = workInfos.joinToString(", ") { it.state.name }
                onStateUpdated(states)
            }
        }
}
