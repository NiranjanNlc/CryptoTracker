# Price Alert Notification System

This implementation enhances the WorkManager task to check stored alerts against real-time cryptocurrency prices and trigger notifications when alert thresholds are crossed.

## Components

1. **Enhanced CryptoPriceWorker**: The existing WorkManager task has been enhanced to:
   - Fetch the latest cryptocurrency prices
   - Retrieve alerts from EncryptedSharedPreferences
   - Compare current prices with alert thresholds
   - Trigger notifications when thresholds are crossed

2. **NotificationUtil**: A utility class for handling notifications:
   - Creates and manages notification channels
   - Builds and displays price alert notifications

3. **PermissionUtil**: A utility for handling notification permissions:
   - Checks if notification permission is granted
   - Requests notification permission on Android 13+ devices

4. **AlertTestUtil**: A utility for testing the alert functionality:
   - Creates sample alerts (e.g., Bitcoin > $70,000)
   - Provides methods to add and clear test alerts

5. **WorkManagerTestControls**: Enhanced UI component for testing:
   - Shows the current alert count
   - Provides buttons to add sample alerts
   - Allows clearing all alerts

## How It Works

1. The WorkManager task runs every 5 minutes to fetch the latest cryptocurrency prices.
2. During each run, it retrieves all stored alerts from EncryptedSharedPreferences.
3. It compares each alert's threshold against the current price:
   - For upper bound alerts (isUpperBound = true), it checks if the price is greater than or equal to the threshold.
   - For lower bound alerts (isUpperBound = false), it checks if the price is less than or equal to the threshold.
4. When an alert threshold is crossed, a notification is triggered.
5. The notification displays the cryptocurrency name, symbol, threshold, and current price.

## Testing

1. Use the enhanced WorkManagerTestControls UI to add sample alerts:
   - "Add BTC Alert" adds a Bitcoin alert with a threshold of $70,000
   - "Add Multiple" adds several sample alerts for different cryptocurrencies
   - "Clear All Alerts" removes all alerts

2. Manually trigger a price update using the "Update Prices Now" button to test alerts immediately.

3. Check the logcat for messages from "CryptoPriceWorker" to see alert processing details.

## Permissions

The app requests the POST_NOTIFICATIONS permission on Android 13+ devices. If the user denies this permission, they will not receive price alert notifications.
