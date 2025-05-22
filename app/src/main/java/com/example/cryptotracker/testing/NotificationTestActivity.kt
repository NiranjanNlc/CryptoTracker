package com.example.cryptotracker.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cryptotracker.CryptoTrackerApplication
import com.example.cryptotracker.data.util.SecurePreferencesManager
import com.example.cryptotracker.ui.theme.CryptoTrackerTheme

/**
 * Activity for testing notification threshold crossing
 * This activity provides a more advanced UI to run tests for verifying
 * that price alerts trigger notifications correctly when thresholds are crossed.
 */
class NotificationTestActivity : ComponentActivity() {
    
    private lateinit var notificationTester: NotificationThresholdManualTest
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the notification tester
        val repository = CryptoTrackerApplication.getRepository()
        val preferencesManager = SecurePreferencesManager(this)
        notificationTester = NotificationThresholdManualTest(this, repository, preferencesManager)
        
        setContent {
            CryptoTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdvancedNotificationTestScreen(
                        onRunTest = { symbol, name, initialPrice, targetPrice, isUpperBound ->
                            // Use the NotificationTestRunner for more controlled testing
                            NotificationTestRunner.runTest(
                                context = this,
                                cryptoSymbol = symbol,
                                cryptoName = name,
                                initialPrice = initialPrice,
                                targetPrice = targetPrice,
                                isUpperBound = isUpperBound
                            )
                        },
                        onStopTest = {
                            NotificationTestRunner.stopTest(this)
                        },
                        onClearAlerts = {
                            notificationTester.clearAllAlerts()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedNotificationTestScreen(
    onRunTest: (String, String, Double, Double, Boolean) -> Unit,
    onStopTest: () -> Unit,
    onClearAlerts: () -> Unit
) {
    var cryptoSymbol by remember { mutableStateOf("BTC") }
    var cryptoName by remember { mutableStateOf("Bitcoin") }
    var initialPrice by remember { mutableDoubleStateOf(50000.0) }
    var targetPrice by remember { mutableDoubleStateOf(52500.0) }
    var isUpperBound by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Notification Threshold Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Test Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = cryptoSymbol,
                    onValueChange = { cryptoSymbol = it },
                    label = { Text("Crypto Symbol") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = cryptoName,
                    onValueChange = { cryptoName = it },
                    label = { Text("Crypto Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = initialPrice.toString(),
                    onValueChange = { 
                        initialPrice = it.toDoubleOrNull() ?: initialPrice
                    },
                    label = { Text("Initial Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = targetPrice.toString(),
                    onValueChange = { 
                        targetPrice = it.toDoubleOrNull() ?: targetPrice
                    },
                    label = { Text("Target Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                Text(
                    text = "Alert Type:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = isUpperBound,
                        onClick = { isUpperBound = true }
                    )
                    Text(
                        text = "Upper Bound (alert when price goes above target)",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = !isUpperBound,
                        onClick = { isUpperBound = false }
                    )
                    Text(
                        text = "Lower Bound (alert when price goes below target)",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        
        Button(
            onClick = {
                onRunTest(cryptoSymbol, cryptoName, initialPrice, targetPrice, isUpperBound)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Run Test")
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text(
            text = "Test Controls",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Button(
            onClick = onStopTest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Stop All Simulations")
        }
        
        Button(
            onClick = onClearAlerts,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Clear All Alerts")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How to Test",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = "1. Configure the test parameters above\n" +
                           "2. Click 'Run Test' to start the simulation\n" +
                           "3. Wait for the notification to appear\n" +
                           "4. Use 'Stop All Simulations' when done",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
