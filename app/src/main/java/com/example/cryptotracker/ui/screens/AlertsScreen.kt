package com.example.cryptotracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.model.Alert
import com.example.cryptotracker.model.CryptoCurrency
import com.example.cryptotracker.navigation.NavDestinations
import com.example.cryptotracker.ui.theme.CryptoGreen
import com.example.cryptotracker.ui.theme.CryptoRed
import com.example.cryptotracker.ui.viewmodel.AlertViewModel
import com.example.cryptotracker.util.AlertConverter
import android.widget.Toast
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController = rememberNavController(),
    viewModel: AlertViewModel = viewModel(factory = AlertViewModel.Factory())
) {
    // Collect alerts from the ViewModel
    val alerts by viewModel.alerts.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Refresh alerts when screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshAlerts()
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
                    coroutineScope.launch {
                        val success = viewModel.deleteAlert(alertToDelete.id)
                        if (success) {
                            Toast.makeText(context, "Alert deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete alert", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onToggleAlert = { alert, isEnabled ->
                    // Toggle the alert using the ViewModel
                    coroutineScope.launch {
                        val success = viewModel.toggleAlertEnabled(alert.id, isEnabled)
                        if (!success) {
                            Toast.makeText(context, "Failed to update alert", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun AlertsList(
    alerts: List<Alert>,
    onDeleteAlert: (Alert) -> Unit = {},
    onToggleAlert: (Alert, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(
            items = alerts,
            key = { it.id }
        ) { alert ->
            var isDeleted by remember { mutableStateOf(false) }
            
            AnimatedVisibility(
                visible = !isDeleted,
                exit = shrinkHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                SwipeToDeleteItem(
                    onDelete = {
                        isDeleted = true
                        onDeleteAlert(alert)
                    }
                ) {
                    AlertItem(
                        alert = alert,
                        onDelete = {
                            isDeleted = true
                            onDeleteAlert(alert)
                        },
                        onToggle = { isEnabled -> onToggleAlert(alert, isEnabled) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var offsetX by remember { mutableStateOf(0f) }
    val threshold = with(density) { -100.dp.toPx() }
    
    Box {
        // Background (shown when swiping)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        
        // Foreground (the actual item)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Only allow swiping left (negative delta)
                        if (delta < 0 || offsetX < 0) {
                            offsetX += delta
                        }
                        
                        // If swiped far enough, trigger delete
                        if (offsetX < threshold) {
                            onDelete()
                            offsetX = 0f
                        }
                    },
                    onDragStopped = {
                        // Spring back if not deleted
                        offsetX = 0f
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
fun DismissBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertItem(
    alert: Alert,
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
                    val alertText = if (alert.isUpperBound) {
                        "Above $${alert.threshold}"
                    } else {
                        "Below $${alert.threshold}"
                    }
                    
                    val alertColor = if (alert.isUpperBound) CryptoGreen else CryptoRed
                    
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Price Alerts",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create alerts to get notified when cryptocurrency prices reach your target",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun AlertsScreenPreview() {
    AlertsScreen()
}