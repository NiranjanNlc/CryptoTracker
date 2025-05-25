# CryptoTracker Use Case Diagram

```
+-------------------------------------------------------------------------------------------------------------+
|                                           CryptoTracker System                                              |
+-------------------------------------------------------------------------------------------------------------+
|                                                                                                             |
|  +-------------+                                                                                            |
|  |             |                                                                                            |
|  |    User     |                                                                                            |
|  |             |                                                                                            |
|  +------+------+                                                                                            |
|         |                                                                                                   |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | View Cryptocurrency     |                                                                |
|         |        | Prices                  |                                                                |
|         |        +-------------------------+                                                                |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | View Detailed Crypto    |                                                                |
|         |        | Information             |                                                                |
|         |        +-------------------------+                                                                |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | Set Price Alerts        | ------+                                                        |
|         |        |                         |       |                                                        |
|         |        +-------------------------+       |                                                        |
|         |                                          |                                                        |
|         |                                          v                                                        |
|         |                                  +-------------------------+                                      |
|         |                                  | Receive Price Alert     |                                      |
|         |                                  | Notifications           |                                      |
|         |                                  +-------------------------+                                      |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | Manage Existing Alerts  |                                                                |
|         |        | (Enable/Disable/Delete) |                                                                |
|         |        +-------------------------+                                                                |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+       +-------------------------+                              |
|         |        | Configure Price         | ----> | Random Mode             |                              |
|         |        | Simulation              |       +-------------------------+                              |
|         |        |                         |                                                                |
|         |        |                         | ----> +-------------------------+                              |
|         |        |                         |       | Uptrend Mode            |                              |
|         |        |                         |       +-------------------------+                              |
|         |        |                         |                                                                |
|         |        |                         | ----> +-------------------------+                              |
|         |        |                         |       | Downtrend Mode          |                              |
|         |        |                         |       +-------------------------+                              |
|         |        |                         |                                                                |
|         |        |                         | ----> +-------------------------+                              |
|         |        |                         |       | Volatile Mode           |                              |
|         |        |                         |       +-------------------------+                              |
|         |        |                         |                                                                |
|         |        |                         | ----> +-------------------------+                              |
|         |        |                         |       | Stable Mode             |                              |
|         |        +-------------------------+       +-------------------------+                              |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|                  | Start/Stop Simulation   |                                                                |
|                  |                         |                                                                |
|                  +-------------------------+                                                                |
|                                                                                                             |
|                                                                                                             |
|  +-------------+                                                                                            |
|  |             |                                                                                            |
|  |   System    |                                                                                            |
|  |             |                                                                                            |
|  +------+------+                                                                                            |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | Fetch Real Crypto Data  |                                                                |
|         |        | from API                |                                                                |
|         |        +-------------------------+                                                                |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | Cache Cryptocurrency    |                                                                |
|         |        | Data                    |                                                                |
|         |        +-------------------------+                                                                |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|         |        | Monitor Price           |                                                                |
|         |        | Thresholds              |                                                                |
|         |        +-------------------------+                                                                |
|         |                                                                                                   |
|         |                                                                                                   |
|         +------> +-------------------------+                                                                |
|                  | Simulate Price Changes  |                                                                |
|                  |                         |                                                                |
|                  +-------------------------+                                                                |
|                                                                                                             |
+-------------------------------------------------------------------------------------------------------------+
```

## Description of Use Cases

### User Actor Use Cases:

1. **View Cryptocurrency Prices**
   - User can view a list of cryptocurrencies with their current prices
   - Includes price changes and percentage movements

2. **View Detailed Crypto Information**
   - User can select a specific cryptocurrency to view detailed information
   - Includes historical data and additional market information

3. **Set Price Alerts**
   - User can create new price alerts for specific cryptocurrencies
   - User can set threshold values (above/below certain price)

4. **Receive Price Alert Notifications**
   - User receives notifications when cryptocurrency prices cross their set thresholds

5. **Manage Existing Alerts**
   - User can view all their active alerts
   - User can enable/disable specific alerts
   - User can delete alerts they no longer need

6. **Configure Price Simulation**
   - User can configure simulation parameters
   - User can select different simulation modes:
     - Random Mode: Random price fluctuations
     - Uptrend Mode: Generally increasing prices
     - Downtrend Mode: Generally decreasing prices
     - Volatile Mode: Large price swings
     - Stable Mode: Minimal price changes

7. **Start/Stop Simulation**
   - User can start the price simulation
   - User can stop the simulation and return to real data

### System Actor Use Cases:

1. **Fetch Real Crypto Data from API**
   - System retrieves cryptocurrency data from external APIs
   - Updates prices and market information

2. **Cache Cryptocurrency Data**
   - System stores cryptocurrency data locally
   - Provides data when offline or between API refreshes

3. **Monitor Price Thresholds**
   - System continuously checks if prices cross user-defined thresholds
   - Triggers notifications when thresholds are crossed

4. **Simulate Price Changes**
   - System generates simulated price changes based on selected mode
   - Updates UI with simulated prices

## Relationships and Extensions

- "Set Price Alerts" extends to "Receive Price Alert Notifications" when price thresholds are met
- "Configure Price Simulation" includes selecting from the five different simulation modes
- "View Cryptocurrency Prices" is a prerequisite for "View Detailed Crypto Information"
- "Fetch Real Crypto Data from API" and "Simulate Price Changes" are mutually exclusive - the system is either using real data or simulated data
