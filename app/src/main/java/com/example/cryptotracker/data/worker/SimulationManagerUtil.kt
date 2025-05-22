package com.example.cryptotracker.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cryptotracker.data.repository.CryptoRepository
import java.util.concurrent.TimeUnit

/**
 * Utility class for managing cryptocurrency price simulation
 */
class SimulationManagerUtil(
    private val context: Context,
    private val repository: CryptoRepository
) {
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val TAG = "SimulationManagerUtil"
        
        // Default interval for periodic simulation (1 minute)
        private const val DEFAULT_SIMULATION_INTERVAL_MINUTES = 1L
    }
    
    /**
     * Schedule a one-time simulation of cryptocurrency prices
     * 
     * @param simulationMode The simulation mode (random, uptrend, downtrend, volatile, stable)
     * @param volatility The maximum percentage by which prices can change
     */
    fun runOneTimeSimulation(
        simulationMode: String = CryptoSimulationWorker.MODE_RANDOM,
        volatility: Float = CryptoSimulationWorker.DEFAULT_VOLATILITY
    ) {
        Log.i(TAG, "Scheduling one-time simulation with mode: $simulationMode, volatility: $volatility%")
        
        // Create input data for the worker
        val inputData = Data.Builder()
            .putString(CryptoSimulationWorker.KEY_SIMULATION_MODE, simulationMode)
            .putFloat(CryptoSimulationWorker.KEY_VOLATILITY, volatility)
            .build()
        
        // Create a one-time work request
        val workRequest = OneTimeWorkRequestBuilder<CryptoSimulationWorker>()
            .setInputData(inputData)
            .addTag(CryptoSimulationWorker.TAG)
            .build()
        
        // Enqueue the work request
        workManager.enqueueUniqueWork(
            CryptoSimulationWorker.WORK_NAME + "_onetime",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.i(TAG, "One-time simulation scheduled successfully")
    }
    
    /**
     * Schedule periodic simulation of cryptocurrency prices
     * 
     * @param simulationMode The simulation mode (random, uptrend, downtrend, volatile, stable)
     * @param volatility The maximum percentage by which prices can change
     * @param intervalMinutes The interval between simulations in minutes
     */
    fun schedulePeriodicSimulation(
        simulationMode: String = CryptoSimulationWorker.MODE_RANDOM,
        volatility: Float = CryptoSimulationWorker.DEFAULT_VOLATILITY,
        intervalMinutes: Long = DEFAULT_SIMULATION_INTERVAL_MINUTES
    ) {
        Log.i(TAG, "Scheduling periodic simulation every $intervalMinutes minutes with mode: $simulationMode, volatility: $volatility%")
        
        // Create input data for the worker
        val inputData = Data.Builder()
            .putString(CryptoSimulationWorker.KEY_SIMULATION_MODE, simulationMode)
            .putFloat(CryptoSimulationWorker.KEY_VOLATILITY, volatility)
            .build()
        
        // Define constraints - we don't need network for simulation
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        // Create a periodic work request
        val workRequest = PeriodicWorkRequestBuilder<CryptoSimulationWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(CryptoSimulationWorker.TAG)
            .build()
        
        // Enqueue the work request, replacing any existing one
        workManager.enqueueUniquePeriodicWork(
            CryptoSimulationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Log.i(TAG, "Periodic simulation scheduled successfully")
    }
    
    /**
     * Cancel scheduled periodic simulation
     */
    fun cancelPeriodicSimulation() {
        Log.i(TAG, "Cancelling scheduled periodic simulation")
        workManager.cancelUniqueWork(CryptoSimulationWorker.WORK_NAME)
    }
    
    /**
     * Cancel any one-time simulation
     */
    fun cancelOneTimeSimulation() {
        Log.i(TAG, "Cancelling one-time simulation")
        workManager.cancelUniqueWork(CryptoSimulationWorker.WORK_NAME + "_onetime")
    }
    
    /**
     * Cancel all simulations (both periodic and one-time)
     */
    fun cancelAllSimulations() {
        cancelPeriodicSimulation()
        cancelOneTimeSimulation()
    }
}
