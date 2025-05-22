package com.example.cryptotracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cryptotracker.navigation.NavDestinations
import com.example.cryptotracker.ui.screens.AlertsScreen
import com.example.cryptotracker.ui.screens.AlertSetupScreen
import com.example.cryptotracker.ui.screens.CryptoDetailScreen
import com.example.cryptotracker.ui.screens.PricesScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTrackerApp() {
    val navController = rememberNavController()
    
    // Define bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem(
            route = NavDestinations.Prices.route,
            title = "Prices",
            // icon for chart
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = NavDestinations.Alerts.route,
            title = "Alerts",
            icon = Icons.Default.Notifications
        )
    )
    
    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        text = "CryptoTracker",
//                        style = MaterialTheme.typography.headlineSmall
//                    )
//                },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimary
//                )
//            )
//        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Text(
                                text = item.title,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestinations.Prices.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavDestinations.Prices.route) {
                PricesScreen(navController = navController)
            }
            composable(NavDestinations.Alerts.route) {
                AlertsScreen(navController = navController)
            }
            composable(NavDestinations.AlertSetup.route) {
                AlertSetupScreen(navController = navController)
            }
            composable(
                route = NavDestinations.CryptoDetail.route,
                arguments = listOf(
                    navArgument("cryptoId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val cryptoId = backStackEntry.arguments?.getString("cryptoId") ?: ""
                CryptoDetailScreen(
                    cryptoId = cryptoId,
                    navController = navController
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Prices Screen Preview")
@Composable
fun PreviewPricesScreen() {
    PricesScreen()
}

@Preview(showBackground = true, name = "Alerts Screen Preview")
@Composable
fun PreviewAlertsScreen() {
    AlertsScreen(navController = rememberNavController())
}