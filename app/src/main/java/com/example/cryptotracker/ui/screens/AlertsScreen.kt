package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.model.CryptoAlert
import com.example.cryptotracker.navigation.NavDestinations
import com.example.cryptotracker.ui.theme.CryptoGreen
import com.example.cryptotracker.ui.theme.CryptoRed
import com.example.cryptotracker.ui.viewmodel.AlertViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController = rememberNavController(),
    viewModel: AlertViewModel = viewModel(factory = AlertViewModel.Factory())
) {
    // Collect alerts from the ViewModel
    val alerts by viewModel.alerts.collectAsState()
    
    // Observe for new alerts from the AlertSetupScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val newAlert = savedStateHandle?.get<CryptoAlert>("new_alert")
    
    // Add the new alert to our list if it exists
    LaunchedEffect(newAlert) {
        newAlert?.let { alert ->
            // Add the new alert using the ViewModel
            val crypto = alert.run {
                com.example.cryptotracker.model.CryptoCurrency(
                    id = id,
                    name = cryptoName,
                    symbol = cryptoSymbol,
                    price = targetPrice,
                    priceChangePercentage24h = 0.0,
                    imageUrl = ""
                )
            }
            viewModel.addAlert(crypto, alert.targetPrice, alert.isAboveTarget)
            
            // Clear the saved state to avoid duplicate additions
            savedStateHandle.remove<CryptoAlert>("new_alert")
        }
    }
    
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Price Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavDestinations.AlertSetup.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alert"
                )
            }
        }
    ) { paddingValues ->
        if (alerts.isEmpty()) {
            EmptyAlertsContent()
        } else {
            AlertsList(
                alerts = alerts,
                onDeleteAlert = { alertToDelete ->
                    // Delete the alert using the ViewModel
                    viewModel.deleteAlert(alertToDelete.id)
                },
                onToggleAlert = { alert, isEnabled ->
                    // Toggle the alert using the ViewModel
                    viewModel.toggleAlertEnabled(alert.id, isEnabled)
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun AlertsList(
    alerts: List<CryptoAlert>,
    onDeleteAlert: (CryptoAlert) -> Unit = {},
    onToggleAlert: (CryptoAlert, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(alerts) { alert ->
            AlertItem(
                alert = alert,
                onDelete = { onDeleteAlert(alert) },
                onToggle = { isEnabled -> onToggleAlert(alert, isEnabled) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertItem(
    alert: CryptoAlert,
    onDelete: () -> Unit = {},
    onToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alert icon
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Alert details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alert.cryptoName} (${alert.cryptoSymbol})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val alertText = if (alert.isAboveTarget) {
                        "Above $${alert.targetPrice}"
                    } else {
                        "Below $${alert.targetPrice}"
                    }
                    
                    val alertColor = if (alert.isAboveTarget) CryptoGreen else CryptoRed
                    
                    Text(
                        text = alertText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = alertColor
                    )
                    
                    Switch(
                        checked = alert.isEnabled,
                        onCheckedChange = { onToggle(it) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Delete button
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Alert",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyAlertsContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No alerts set yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tap the + button to create your first price alert",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    AlertsScreen()
}