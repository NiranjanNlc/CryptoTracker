# Notification Testing Framework

This directory contains tools for testing the notification logic in the CryptoTracker app. The testing framework allows you to verify that alerts are triggered correctly when cryptocurrency prices cross defined thresholds.

## Components

### 1. NotificationThresholdManualTest

A utility class that provides methods for manually testing notification thresholds:
- `testUpperBoundAlert()`: Tests notifications for when prices rise above a threshold
- `testLowerBoundAlert()`: Tests notifications for when prices fall below a threshold
- `stopAllSimulations()`: Stops any running simulations
- `clearAllAlerts()`: Clears all saved alerts

### 2. NotificationTestRunner

A more advanced utility that provides a programmatic API for running notification tests:
- `runTest()`: Runs a complete notification test with configurable parameters
- `stopTest()`: Stops all running simulations

### 3. NotificationTestActivity

A user interface for running notification tests directly from the app:
- Configurable test parameters (crypto symbol, name, initial price, target price)
- Support for both upper and lower bound alerts
- Controls for starting and stopping tests

## How to Run Tests

### Using the UI

1. Access the test UI by navigating to the Alerts screen and tapping the menu icon in the top-right corner
2. Select "Test Notifications" from the dropdown menu
3. Configure the test parameters:
   - Crypto Symbol (e.g., "BTC")
   - Crypto Name (e.g., "Bitcoin")
   - Initial Price (starting price for the simulation)
   - Target Price (threshold that should trigger the notification)
   - Alert Type (Upper Bound or Lower Bound)
4. Click "Run Test" to start the simulation
5. Wait for the notification to appear (should happen within a minute)
6. Click "Stop All Simulations" when done

### Using Unit Tests

Run the `NotificationThresholdTest` class in the test directory:
```
./gradlew test --tests "com.example.cryptotracker.NotificationThresholdTest"
```

### Using Instrumentation Tests

Run the `NotificationThresholdInstrumentedTest` class in the androidTest directory:
```
./gradlew connectedAndroidTest --tests "com.example.cryptotracker.NotificationThresholdInstrumentedTest"
```

## Implementation Details

The testing framework leverages the existing simulation system to generate price changes:

1. It creates a test cryptocurrency with an initial price
2. Sets up an alert with the specified threshold
3. Starts a simulation that will cause the price to cross the threshold
4. The notification system should detect the threshold crossing and trigger an alert

This approach allows for comprehensive testing of the notification logic without relying on real API data.
