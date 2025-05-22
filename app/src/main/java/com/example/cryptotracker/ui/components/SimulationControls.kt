package com.example.cryptotracker.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.work.await
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.worker.CryptoSimulationWorker
import com.example.cryptotracker.data.worker.SimulationManagerUtil
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * UI component for controlling the cryptocurrency price simulation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationControls(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Get the SimulationManagerUtil from the application
    val simulationManagerUtil = remember { CryptoTrackerApplication.getSimulationManagerUtil() }
    
    // State for simulation parameters
    var simulationMode by remember { mutableStateOf(CryptoSimulationWorker.MODE_RANDOM) }
    var volatility by remember { mutableFloatStateOf(CryptoSimulationWorker.DEFAULT_VOLATILITY) }
    var isPeriodicEnabled by remember { mutableStateOf(false) }
    var simulationStatus by remember { mutableStateOf("Not running") }
    var isSimulationModeExpanded by remember { mutableStateOf(false) }
    
    // Check if simulation is running
    LaunchedEffect(Unit) {
        updateSimulationStatus(context) { status ->
            simulationStatus = status
            isPeriodicEnabled = status.contains("RUNNING") || status.contains("ENQUEUED")
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Crypto Price Simulation",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Status: $simulationStatus",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simulation mode dropdown
            ExposedDropdownMenuBox(
                expanded = isSimulationModeExpanded,
                onExpandedChange = { isSimulationModeExpanded = it }
            ) {
                TextField(
                    value = getSimulationModeLabel(simulationMode),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Simulation Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSimulationModeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = isSimulationModeExpanded,
                    onDismissRequest = { isSimulationModeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Random") },
                        onClick = {
                            simulationMode = CryptoSimulationWorker.MODE_RANDOM
                            isSimulationModeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Uptrend") },
                        onClick = {
                            simulationMode = CryptoSimulationWorker.MODE_UPTREND
                            isSimulationModeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Downtrend") },
                        onClick = {
                            simulationMode = CryptoSimulationWorker.MODE_DOWNTREND
                            isSimulationModeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Volatile") },
                        onClick = {
                            simulationMode = CryptoSimulationWorker.MODE_VOLATILE
                            isSimulationModeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Stable") },
                        onClick = {
                            simulationMode = CryptoSimulationWorker.MODE_STABLE
                            isSimulationModeExpanded = false
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Volatility slider
            Text(
                text = "Volatility: ${volatility.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Slider(
                value = volatility,
                onValueChange = { volatility = it },
                valueRange = 1f..CryptoSimulationWorker.MAX_VOLATILITY,
                steps = 19
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Periodic simulation toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Periodic Simulation",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = isPeriodicEnabled,
                    onCheckedChange = { checked ->
                        isPeriodicEnabled = checked
                        coroutineScope.launch {
                            if (checked) {
                                simulationManagerUtil.schedulePeriodicSimulation(
                                    simulationMode = simulationMode,
                                    volatility = volatility
                                )
                            } else {
                                simulationManagerUtil.cancelPeriodicSimulation()
                            }
                            updateSimulationStatus(context) { status ->
                                simulationStatus = status
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Run simulation once button
            Button(
                onClick = {
                    coroutineScope.launch {
                        simulationManagerUtil.runOneTimeSimulation(
                            simulationMode = simulationMode,
                            volatility = volatility
                        )
                        updateSimulationStatus(context) { status ->
                            simulationStatus = status
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Simulation Once")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cancel simulation button
            Button(
                onClick = {
                    coroutineScope.launch {
                        simulationManagerUtil.cancelAllSimulations()
                        isPeriodicEnabled = false
                        updateSimulationStatus(context) { status ->
                            simulationStatus = status
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel All Simulations")
            }
        }
    }
}

/**
 * Update the simulation status
 */
private suspend fun updateSimulationStatus(
    context: Context,
    onStatusUpdated: (String) -> Unit
) {
    val workManager = WorkManager.getInstance(context)
    val workInfos = workManager.getWorkInfosByTag(CryptoSimulationWorker.TAG).await()
    
    if (workInfos.isEmpty()) {
        onStatusUpdated("Not running")
        return
    }
    
    val statuses = workInfos.map { it.state.name }
    val runningCount = statuses.count { it == WorkInfo.State.RUNNING.name }
    val enqueuedCount = statuses.count { it == WorkInfo.State.ENQUEUED.name }
    
    when {
        runningCount > 0 -> onStatusUpdated("RUNNING ($runningCount jobs)")
        enqueuedCount > 0 -> onStatusUpdated("ENQUEUED ($enqueuedCount jobs)")
        else -> onStatusUpdated(statuses.joinToString(", "))
    }
}

/**
 * Get a user-friendly label for the simulation mode
 */
private fun getSimulationModeLabel(mode: String): String {
    return when (mode) {
        CryptoSimulationWorker.MODE_RANDOM -> "Random"
        CryptoSimulationWorker.MODE_UPTREND -> "Uptrend"
        CryptoSimulationWorker.MODE_DOWNTREND -> "Downtrend"
        CryptoSimulationWorker.MODE_VOLATILE -> "Volatile"
        CryptoSimulationWorker.MODE_STABLE -> "Stable"
        else -> "Unknown"
    }
}
