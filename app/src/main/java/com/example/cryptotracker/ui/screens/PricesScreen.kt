package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cryptotracker.model.CryptoCurrency
import com.example.cryptotracker.model.CryptoDataProvider
import com.example.cryptotracker.ui.theme.CryptoGreen
import com.example.cryptotracker.ui.theme.CryptoRed

@Composable
fun PricesScreen() {
    val cryptoList = CryptoDataProvider.getMockCryptoList()
    
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
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        
        // Crypto List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(cryptoList) { crypto ->
                CryptoListItem(crypto = crypto)
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CryptoListItem(crypto: CryptoCurrency) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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

// preview screen for prices
@Preview(showBackground = true)
@Composable
fun PreviewCryptoListItem() {
    CryptoListItem(crypto = CryptoDataProvider.getMockCryptoList()[0])
}