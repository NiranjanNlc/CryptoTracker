# CryptoTracker Class Diagram

```
+-----------------------------------------------------------------------------------------------------------+
|                                        CryptoTracker Class Diagram                                         |
+-----------------------------------------------------------------------------------------------------------+

+-------------------+          +------------------------+          +----------------------+
|  CryptoCurrency   |          |         Alert          |          |  PriceHistoryModel   |
+-------------------+          +------------------------+          +----------------------+
| - id: String      |          | - id: String           |          | - timestamp: Long    |
| - name: String    |          | - cryptoSymbol: String |          | - price: Double      |
| - symbol: String  |          | - cryptoName: String   |          | - volume: Double     |
| - price: Double   |          | - threshold: Double    |          | - marketCap: Double  |
| - priceChangePct  |          | - isUpperBound: Boolean|          +----------------------+
| - imageUrl: String|          | - isEnabled: Boolean   |
+-------------------+          | - createdAt: Long      |
                               +------------------------+

+-------------------+
| CryptoDataProvider|
+-------------------+
| + getMockCryptoList()|
+-------------------+

+-------------------+                    +-------------------------+
|  CryptoRepository |<---implements-----| CryptoRepositoryImpl    |
+-------------------+                    +-------------------------+
| + getCryptoPrices()|                   | - coinGeckoApi          |
| + getCryptoById() |                    | - cryptoCache           |
| + updateCachedPrices()|                | - lastCacheUpdateTime   |
+-------------------+                    | + getCryptoPrices()     |
                                         | + getCryptoById()       |
                                         | + updateCachedPrices()  |
                                         | - updateCache()         |
                                         +-------------------------+
                                                     |
                                                     | uses
                                                     v
                                         +-------------------------+
                                         |      CoinGeckoApi       |
                                         +-------------------------+
                                         | + getCryptoMarkets()    |
                                         | + getCryptoById()       |
                                         +-------------------------+

+-------------------+                    +-------------------------+
| CryptoViewModel   |                    |     AlertViewModel      |
+-------------------+                    +-------------------------+
| - repository      |                    | - repository            |
| - cryptoListState |                    | - alertsState           |
| + loadCryptoPrices()|                  | + loadAlerts()          |
| + Factory         |                    | + createAlert()         |
+-------------------+                    | + deleteAlert()         |
                                         | + toggleAlertEnabled()  |
                                         | + Factory               |
                                         +-------------------------+

+-------------------+                    +-------------------------+
|  WorkManagerUtil  |                    | SimulationManagerUtil   |
+-------------------+                    +-------------------------+
| + schedulePriceCheck()|                | - context               |
| + cancelPriceCheck() |                 | - repository            |
+-------------------+                    | + startSimulation()     |
                                         | + stopSimulation()      |
                                         | + isSimulationRunning() |
                                         +-------------------------+
                                                     |
                                                     | schedules
                                                     v
+-------------------+                    +-------------------------+
| CryptoPriceWorker |                    | CryptoSimulationWorker  |
+-------------------+                    +-------------------------+
| + doWork()        |                    | + doWork()              |
| - checkPriceAlerts()|                  | - simulatePriceChanges()|
+-------------------+                    | - updateSimulatedPrices()|
                                         +-------------------------+

+-------------------+                    +-------------------------+
|  PricesScreen     |                    |      AlertsScreen       |
+-------------------+                    +-------------------------+
| + PricesScreen()  |                    | + AlertsScreen()        |
| + CryptoList()    |                    | + AlertsList()          |
| + LoadingState()  |                    | + AlertItem()           |
| + ErrorState()    |                    | + EmptyAlertsContent()  |
+-------------------+                    +-------------------------+

+-------------------+                    +-------------------------+
| CryptoDetailScreen|                    |    AlertSetupScreen     |
+-------------------+                    +-------------------------+
| + DetailScreen()  |                    | + AlertSetupScreen()    |
| + PriceChart()    |                    | + CryptoSelector()      |
| + InfoSection()   |                    | + ThresholdSelector()   |
+-------------------+                    +-------------------------+

+-------------------+                    +-------------------------+
| SimulationControls|                    |  CryptoTrackerApp       |
+-------------------+                    +-------------------------+
| + SimulationControls()|                | + CryptoTrackerApp()    |
| - onModeSelected()|                    | - AppNavHost()          |
| - onVolatilityChanged()|               +-------------------------+
+-------------------+
```

## Class Descriptions

### Data Models
- **CryptoCurrency**: Core data model representing a cryptocurrency with its price information
- **Alert**: Data model representing a price alert set by the user
- **PriceHistoryModel**: Data model for historical price data of cryptocurrencies
- **CryptoDataProvider**: Utility class providing mock cryptocurrency data

### Repository Layer
- **CryptoRepository**: Interface defining methods for accessing cryptocurrency data
- **CryptoRepositoryImpl**: Implementation of the repository interface that fetches data from the API and manages caching
- **CoinGeckoApi**: Interface for the CoinGecko REST API service

### ViewModel Layer
- **CryptoViewModel**: ViewModel for cryptocurrency prices and details screens
- **AlertViewModel**: ViewModel for managing alerts and alert setup

### Worker Classes
- **WorkManagerUtil**: Utility for scheduling background price check operations
- **SimulationManagerUtil**: Utility for managing cryptocurrency price simulation
- **CryptoPriceWorker**: Worker for checking cryptocurrency prices against alert thresholds
- **CryptoSimulationWorker**: Worker for simulating cryptocurrency price changes

### UI Components
- **PricesScreen**: Main screen showing cryptocurrency prices
- **AlertsScreen**: Screen for viewing and managing price alerts
- **CryptoDetailScreen**: Screen showing detailed information about a cryptocurrency
- **AlertSetupScreen**: Screen for creating new price alerts
- **SimulationControls**: UI component for controlling price simulation
- **CryptoTrackerApp**: Main composable function defining the app structure and navigation

## Relationships

- **CryptoRepositoryImpl** implements **CryptoRepository** and uses **CoinGeckoApi**
- **CryptoViewModel** and **AlertViewModel** use **CryptoRepository**
- **SimulationManagerUtil** schedules **CryptoSimulationWorker**
- **WorkManagerUtil** schedules **CryptoPriceWorker**
- UI screens use their respective ViewModels
- **CryptoTrackerApp** coordinates navigation between all screens
