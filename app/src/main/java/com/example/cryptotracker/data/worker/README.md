# WorkManager Implementation for CryptoTracker

This implementation uses WorkManager to schedule periodic background updates of cryptocurrency prices every 5 minutes.

## Components

1. **CryptoPriceWorker.kt**: A CoroutineWorker that fetches cryptocurrency prices in the background and updates the cache.
2. **WorkManagerUtil.kt**: Utility class for scheduling and managing WorkManager tasks.
3. **WorkManagerTestControls.kt**: UI component for testing and monitoring WorkManager functionality.

## How it Works

- The worker is scheduled to run every 5 minutes with network connectivity constraints.
- When the worker runs, it calls the repository's `getCryptoPrices()` function, which fetches the latest prices and updates the cache.
- If the device is offline, the worker will still run but will use cached data.
- The UI displays the last update time and the current state of the WorkManager task.

## Testing

1. Install and run the app in the emulator.
2. The WorkManager task will be automatically scheduled when the app starts.
3. You can manually trigger a price update by clicking the "Update Prices Now" button.
4. The UI will show the last update time and the current state of the WorkManager task.
5. To verify that the background updates are working:
   - Check the logcat for messages from "CryptoPriceWorker" and "WorkManagerUtil"
   - Watch the "Last Update" time in the UI, which should update every 5 minutes
   - You can force background work to run sooner in Android Studio:
     - Go to Run > Edit Configurations
     - Add `-Pandroid.testInstrumentationRunnerArguments.androidx.work.testing.force_run_all=true` to VM options
