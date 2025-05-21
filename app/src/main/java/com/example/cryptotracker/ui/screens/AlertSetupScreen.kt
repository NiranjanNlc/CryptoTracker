package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.model.CryptoCurrency
import com.example.cryptotracker.navigation.NavDestinations
import com.example.cryptotracker.ui.viewmodel.AlertViewModel
import com.example.cryptotracker.ui.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSetupScreen(
    navController: NavController,
    cryptoViewModel: CryptoViewModel = viewModel(factory = CryptoViewModel.Factory()),
    alertViewModel: AlertViewModel = viewModel(factory = AlertViewModel.Factory())
) {
    // State for the selected cryptocurrency
    var selectedCrypto by remember { mutableStateOf<CryptoCurrency?>(null) }
    
    // State for the dropdown expanded status
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    // State for the price threshold
    var targetPrice by remember { mutableStateOf("") }
    
    // State for the alert type (above/below)
    var isAboveTarget by remember { mutableStateOf(true) }
    
    // Collect available cryptocurrencies from the ViewModel
    val availableCryptos by alertViewModel.availableCryptos.collectAsState()
    val isLoading by alertViewModel.isLoading.collectAsState()
    val error by alertViewModel.error.collectAsState()
    
    // Validation state
    var isFormValid by remember { mutableStateOf(false) }
    
    // Coroutine scope for launching suspend functions
    val coroutineScope = rememberCoroutineScope()

    // Validate form whenever inputs change
    LaunchedEffect(selectedCrypto, targetPrice) {
        isFormValid = selectedCrypto != null && targetPrice.isNotBlank() && 
                      targetPrice.toDoubleOrNull() != null && targetPrice.toDoubleOrNull()!! > 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Alert") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cryptocurrency dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCrypto?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Cryptocurrency") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (dropdownExpanded) 
                                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Dropdown"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableCryptos.forEach { crypto ->
                            DropdownMenuItem(
                                text = { 
                                    Text("${crypto.name} (${crypto.symbol})") 
                                },
                                onClick = {
                                    selectedCrypto = crypto
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Target price input
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = { targetPrice = it },
                    label = { Text("Target Price ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        if (targetPrice.isNotBlank() && targetPrice.toDoubleOrNull() == null) {
                            Text("Please enter a valid number", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                
                // Alert type selection with Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAboveTarget) "Alert when price goes above target" else "Alert when price goes below target",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Switch(
                        checked = isAboveTarget,
                        onCheckedChange = { isAboveTarget = it },
                        thumbContent = {
                            Icon(
                                imageVector = if (isAboveTarget) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Error message if any
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Save alert button
                Button(
                    onClick = {
                        selectedCrypto?.let { crypto ->
                            val priceValue = targetPrice.toDoubleOrNull() ?: 0.0
                            
                            try {
                                // Only use one approach: pass data back to the previous screen
                                // This is the preferred approach as it allows the AlertsScreen to decide
                                // what to do with the data
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "new_alert",
                                    com.example.cryptotracker.model.CryptoAlert(
                                        id = java.util.UUID.randomUUID().toString(),
                                        cryptoName = crypto.name,
                                        cryptoSymbol = crypto.symbol,
                                        targetPrice = priceValue,
                                        isAboveTarget = isAboveTarget
                                    )
                                )
                                
                                // Navigate back
                                navController.navigateUp()
                            } catch (e: Exception) {
                                // Handle any exceptions that might occur
                                error?.let {
                                    // If there's already an error, don't overwrite it
                                } ?: run {
                                    // Show error message
                                    alertViewModel.setError("Failed to save alert: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    enabled = isFormValid
                ) {
                    Text("Save Alert")
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertSetupScreenPreview() {
    AlertSetupScreen(navController = rememberNavController())
}
