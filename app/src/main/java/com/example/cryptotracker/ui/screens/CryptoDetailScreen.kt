package com.example.cryptotracker.ui.screens

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.cryptotracker.model.CryptoDataProvider
import com.example.cryptotracker.model.PriceHistoryGenerator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    cryptoId: String,
    navController: NavController
) {
    // Find the cryptocurrency by ID
    val crypto = remember {
        CryptoDataProvider.getMockCryptoList().find { it.id == cryptoId }
    }
    
    // Generate mock price history data using getMockPriceHistory
    val priceHistory = remember {
        crypto?.let {
            PriceHistoryGenerator.getMockPriceHistory(it.symbol)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = crypto?.name ?: "Cryptocurrency Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (crypto != null) {
                // Cryptocurrency info section
                CryptoInfoSection(crypto)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Chart section
                Text(
                    text = "Price History (24h)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Chart container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // MPAndroidChart integration using ComposeView
                        priceHistory?.let { history ->
                            PriceChart(
                                entries = history.map { 
                                    Entry(
                                        it.time.toFloat(), 
                                        it.price.toFloat()
                                    ) 
                                },
                                cryptoSymbol = crypto.symbol
                            )
                        }
                    }
                }
            } else {
                // Error state if cryptocurrency not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cryptocurrency not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CryptoInfoSection(crypto: com.example.cryptotracker.model.CryptoCurrency) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = crypto.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = crypto.symbol,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", crypto.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Price change with color
                    val priceChangeText = if (crypto.priceChangePercentage24h >= 0) {
                        "+${String.format("%.2f", crypto.priceChangePercentage24h)}%"
                    } else {
                        "${String.format("%.2f", crypto.priceChangePercentage24h)}%"
                    }
                    
                    val priceChangeColor = if (crypto.priceChangePercentage24h >= 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    
                    Text(
                        text = priceChangeText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = priceChangeColor
                    )
                }
            }
        }
    }
}

@Composable
fun PriceChart(entries: List<Entry>, cryptoSymbol: String) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    var textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    var gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()
    
    // Use AndroidView to integrate MPAndroidChart
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Create LineChart
            LineChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Configure chart appearance
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                
                // Configure X axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = textColor
                    setDrawGridLines(true)
                    gridColor = gridColor
                    valueFormatter = object : ValueFormatter() {
                        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        
                        override fun getFormattedValue(value: Float): String {
                            return dateFormat.format(Date(value.toLong()))
                        }
                    }
                    labelRotationAngle = 45f
                    granularity = 3600000f // 1 hour in milliseconds
                }
                
                // Configure left Y axis
                axisLeft.apply {
                    textColor = textColor
                    setDrawGridLines(true)
                    gridColor = gridColor
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "$${"%.2f".format(value)}"
                        }
                    }
                }
                
                // Disable right Y axis
                axisRight.isEnabled = false
                
                // Configure legend
                legend.apply {
                    textColor = textColor
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }
                
                // Add some padding
                setExtraOffsets(10f, 10f, 10f, 10f)
            }
        },
        update = { chart ->
            // Create dataset from entries
            val dataSet = LineDataSet(entries, "$cryptoSymbol Price").apply {
                color = primaryColor
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                
                // Fill under the line
                setDrawFilled(true)
                fillAlpha = 50
                fillColor = primaryColor
                
                // Highlight
                highLightColor = primaryColor
                setDrawHighlightIndicators(true)
                highlightLineWidth = 1.5f
            }
            
            // Set data to chart
            chart.data = LineData(dataSet)
            
            // Animate the chart
            chart.animateX(1000)
            
            // Refresh chart
            chart.invalidate()
        }
    )
}
