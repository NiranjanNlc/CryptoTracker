package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.model.CryptoCurrency
import com.example.cryptotracker.model.CryptoDataProvider
import com.example.cryptotracker.navigation.NavDestinations
import com.example.cryptotracker.ui.components.CacheTestControls
import com.example.cryptotracker.ui.components.SimulationControls
import com.example.cryptotracker.ui.components.WorkManagerTestControls
import com.example.cryptotracker.ui.theme.CryptoGreen
import com.example.cryptotracker.ui.theme.CryptoRed
import com.example.cryptotracker.ui.viewmodel.CryptoListState
import com.example.cryptotracker.ui.viewmodel.CryptoViewModel

@Composable
fun PricesScreen(
    navController: NavController = rememberNavController(),
    viewModel: CryptoViewModel = viewModel(factory = CryptoViewModel.Factory())
) {
    // Collect state from ViewModel
    val cryptoListState by viewModel.cryptoListState.collectAsState()
    val scrollState = rememberScrollState()
    
    // State to control visibility of simulation controls
    var showSimulationControls by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cryptocurrency Prices",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Toggle for simulation controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Simulation Mode",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = showSimulationControls,
                onCheckedChange = { showSimulationControls = it }
            )
        }
        
        // Show simulation controls if enabled
        if (showSimulationControls) {
            SimulationControls(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Content based on state
        when (cryptoListState) {
            is CryptoListState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingState()
                }
            }

            is CryptoListState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Cache Testing Controls
                    CacheTestControls(
                        onRefresh = { viewModel.loadCryptoPrices() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // WorkManager Testing Controls
                    WorkManagerTestControls(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    val cryptoList = (cryptoListState as CryptoListState.Success).data
                    CryptoListNonScrollable(
                        cryptoList = cryptoList,
                        onCryptoClick = { cryptoId ->
                            navController.navigate(NavDestinations.CryptoDetail.createRoute(cryptoId))
                        }
                    )
                }
            }

            is CryptoListState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val errorMessage = (cryptoListState as CryptoListState.Error).message
                    ErrorState(
                        errorMessage = errorMessage,
                        onRetry = { viewModel.loadCryptoPrices() }
                    )
                }
            }
        }
    }
}

@Composable
fun CryptoListNonScrollable(
    cryptoList: List<CryptoCurrency>,
    onCryptoClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        cryptoList.forEach { crypto ->
            CryptoListItem(
                crypto = crypto,
                onClick = { onCryptoClick(crypto.id) }
            )
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading cryptocurrency data...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Filled.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Unable to Load Data",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
fun CryptoListItem(
    crypto: CryptoCurrency,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left section - Name and Symbol
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = crypto.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = crypto.symbol,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Right section - Price and Change
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$${String.format("%.2f", crypto.price)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Price change with color
            val priceChangeText = if (crypto.priceChangePercentage24h >= 0) {
                "+${String.format("%.2f", crypto.priceChangePercentage24h)}%"
            } else {
                "${String.format("%.2f", crypto.priceChangePercentage24h)}%"
            }
            
            val priceChangeColor = if (crypto.priceChangePercentage24h >= 0) {
                CryptoGreen
            } else {
                CryptoRed
            }
            
            Text(
                text = priceChangeText,
                style = MaterialTheme.typography.bodyMedium,
                color = priceChangeColor
            )
        }
    }
}

// Preview for crypto list item
@Preview(showBackground = true)
@Composable
fun PreviewCryptoListItem() {
    CryptoListItem(
        crypto = CryptoDataProvider.getMockCryptoList().first()
    )
}