# CryptoTracker Architecture Diagram

```
+--------------------------------------------------------------------------------------------------------------+
|                                       CryptoTracker Architecture                                              |
+--------------------------------------------------------------------------------------------------------------+

+------------------------------------------------------------------------------------------------------+
|                                               UI Layer                                               |
+------------------------------------------------------------------------------------------------------+
|                                                                                                      |
|  +-------------------+    +-------------------+    +-------------------+    +-------------------+    |
|  |   PricesScreen    |    |  CryptoDetailScreen |  |    AlertsScreen   |    | AlertSetupScreen  |    |
|  +-------------------+    +-------------------+    +-------------------+    +-------------------+    |
|                                                                                                      |
|  +-------------------+    +-------------------+    +-------------------+                             |
|  |SimulationControls |    |CacheTestControls  |    |WorkManagerTestControls                          |
|  +-------------------+    +-------------------+    +-------------------+                             |
|                                                                                                      |
+------------------------------------------------------------------------------------------------------+
                 ^                                               ^
                 |                                               |
                 | State                                         | Events
                 |                                               |
+------------------------------------------------------------------------------------------------------+
|                                           ViewModel Layer                                            |
+------------------------------------------------------------------------------------------------------+
|                                                                                                      |
|            +-------------------+                          +-------------------+                      |
|            |  CryptoViewModel  |                          |  AlertViewModel   |                      |
|            +-------------------+                          +-------------------+                      |
|                                                                                                      |
+------------------------------------------------------------------------------------------------------+
                 ^                                               ^
                 |                                               |
                 | Data                                          | Data
                 |                                               |
+------------------------------------------------------------------------------------------------------+
|                                           Repository Layer                                           |
+------------------------------------------------------------------------------------------------------+
|                                                                                                      |
|            +-------------------+                          +-------------------+                      |
|            | CryptoRepository  |                          |  AlertRepository  |                      |
|            +-------------------+                          +-------------------+                      |
|                     ^                                              ^                                 |
|                     |                                              |                                 |
+------------------------------------------------------------------------------------------------------+
        ^                    ^                                ^                    ^
        |                    |                                |                    |
        | Network            | Cache                          | Database           | Preferences
        |                    |                                |                    |
+------------------------------------------------------------------------------------------------------+
|                                             Data Layer                                               |
+------------------------------------------------------------------------------------------------------+
|                                                                                                      |
|  +-------------------+    +-------------------+    +-------------------+    +-------------------+    |
|  |   CoinGeckoApi    |    |   CryptoCache     |    |   AlertDatabase   |    |SharedPreferences  |    |
|  +-------------------+    +-------------------+    +-------------------+    +-------------------+    |
|                                                                                                      |
+------------------------------------------------------------------------------------------------------+
                                                ^
                                                |
                                                | Background Processing
                                                |
+------------------------------------------------------------------------------------------------------+
|                                         Background Workers                                           |
+------------------------------------------------------------------------------------------------------+
|                                                                                                      |
|  +-------------------+                                      +-------------------+                    |
|  | CryptoPriceWorker |                                      |CryptoSimulationWorker                  |
|  +-------------------+                                      +-------------------+                    |
|                                                                                                      |
|  +-------------------+                                      +-------------------+                    |
|  |  WorkManagerUtil  |                                      |SimulationManagerUtil                   |
|  +-------------------+                                      +-------------------+                    |
|                                                                                                      |
+------------------------------------------------------------------------------------------------------+
```

## Architecture Overview

The CryptoTracker application follows the MVVM (Model-View-ViewModel) architecture pattern with a clean separation of concerns between different layers. This architecture promotes testability, maintainability, and scalability.

### UI Layer
The UI layer is built using Jetpack Compose and consists of various screens and reusable UI components:
- **PricesScreen**: Displays a list of cryptocurrencies with their current prices
- **CryptoDetailScreen**: Shows detailed information about a selected cryptocurrency
- **AlertsScreen**: Displays and manages price alerts
- **AlertSetupScreen**: Interface for creating new price alerts
- **SimulationControls**: UI component for controlling the price simulation feature
- **Test Controls**: Components for testing cache and WorkManager functionality

The UI layer observes state from the ViewModel layer and sends user events back to it.

### ViewModel Layer
The ViewModel layer serves as a bridge between the UI and data layers:
- **CryptoViewModel**: Manages cryptocurrency data and UI state for prices and details screens
- **AlertViewModel**: Manages alert data and UI state for alerts-related screens

ViewModels expose state as Kotlin Flows and provide methods for handling user actions.

### Repository Layer
The Repository layer abstracts the data sources and provides a clean API for the ViewModel layer:
- **CryptoRepository**: Manages cryptocurrency data, including fetching from API and caching
- **AlertRepository**: Manages alert data, including database operations

### Data Layer
The Data layer consists of various data sources:
- **CoinGeckoApi**: REST API service for fetching cryptocurrency data
- **CryptoCache**: In-memory cache for cryptocurrency data
- **AlertDatabase**: Local database for storing price alerts
- **SharedPreferences**: For storing user preferences and settings

### Background Workers
The application uses WorkManager for background processing:
- **CryptoPriceWorker**: Periodically checks cryptocurrency prices against alert thresholds
- **CryptoSimulationWorker**: Simulates cryptocurrency price changes for testing
- **WorkManagerUtil**: Utility for scheduling and managing price check workers
- **SimulationManagerUtil**: Utility for managing simulation workers

## Key Features

### Real-time Cryptocurrency Prices
- Fetches real-time cryptocurrency data from the CoinGecko API
- Displays prices, price changes, and other relevant information
- Provides detailed view for individual cryptocurrencies

### Price Alerts
- Allows users to set price threshold alerts for cryptocurrencies
- Monitors prices in the background and sends notifications when thresholds are crossed
- Provides management interface for enabling, disabling, and deleting alerts

### Price Simulation
- Simulates cryptocurrency price changes for testing and demonstration
- Supports different simulation modes (random, uptrend, downtrend, volatile, stable)
- Allows configuration of volatility and other simulation parameters

### Caching
- Implements caching mechanism for cryptocurrency data
- Provides offline access to previously fetched data
- Optimizes performance and reduces API calls

## Data Flow

1. **UI Layer** displays data and captures user interactions
2. **ViewModel Layer** processes user actions and manages UI state
3. **Repository Layer** coordinates data operations between different sources
4. **Data Layer** provides access to remote and local data
5. **Background Workers** perform periodic tasks and update data as needed

This architecture ensures a clean separation of concerns, making the application more maintainable, testable, and scalable.
