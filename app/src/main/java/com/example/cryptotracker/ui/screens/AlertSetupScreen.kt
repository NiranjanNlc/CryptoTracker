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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.model.Alert
import com.example.cryptotracker.model.CryptoCurrency
import com.example.cryptotracker.navigation.NavDestinations
import com.example.cryptotracker.ui.viewmodel.AlertViewModel
import com.example.cryptotracker.ui.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import android.widget.Toast

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
    
    // Error message state
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // Context for showing toast messages
    val context = LocalContext.current
    
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
                            .menuAnchor(),
                        isError = validationError != null && selectedCrypto == null,
                        supportingText = {
                            if (validationError != null && selectedCrypto == null) {
                                Text("Please select a cryptocurrency", color = MaterialTheme.colorScheme.error)
                            }
                        }
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
                                    validationError = null
                                }
                            )
                        }
                    }
                }
                
                // Target price input
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = { 
                        targetPrice = it
                        validationError = null
                    },
                    label = { Text("Target Price ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = validationError != null && (targetPrice.isBlank() || targetPrice.toDoubleOrNull() == null || targetPrice.toDoubleOrNull()!! <= 0),
                    supportingText = {
                        when {
                            validationError != null && targetPrice.isBlank() -> {
                                Text("Please enter a price", color = MaterialTheme.colorScheme.error)
                            }
                            validationError != null && targetPrice.toDoubleOrNull() == null -> {
                                Text("Please enter a valid number", color = MaterialTheme.colorScheme.error)
                            }
                            validationError != null && targetPrice.toDoubleOrNull() != null && targetPrice.toDoubleOrNull()!! <= 0 -> {
                                Text("Price must be greater than 0", color = MaterialTheme.colorScheme.error)
                            }
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
                        // Validate inputs
                        if (selectedCrypto == null) {
                            validationError = "Please select a cryptocurrency"
                            return@Button
                        }
                        
                        val priceValue = targetPrice.toDoubleOrNull()
                        if (priceValue == null || priceValue <= 0) {
                            validationError = "Please enter a valid price greater than 0"
                            return@Button
                        }
                        
                        selectedCrypto?.let { crypto ->
                            try {
                                // Create a new Alert and save it directly using the ViewModel
                                val success = alertViewModel.addAlert(crypto, priceValue, isAboveTarget)
                                
                                if (success) {
                                    // Show success toast
                                    Toast.makeText(context, "Alert created successfully", Toast.LENGTH_SHORT).show()
                                    
                                    // Navigate back to the alerts screen
                                    navController.navigateUp()
                                } else {
                                    // Show error toast
                                    Toast.makeText(context, "Failed to save alert", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                alertViewModel.setError("Failed to create alert: ${e.message}")
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Create Alert")
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

@Preview
@Composable
fun AlertSetupScreenPreview() {
    AlertSetupScreen(navController = rememberNavController())
}
