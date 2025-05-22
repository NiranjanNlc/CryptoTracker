package com.example.cryptotracker.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Utility class for handling runtime permissions
 */
object PermissionUtil {
    
    /**
     * Check if notification permission is granted
     * 
     * @param context Application context
     * @return true if permission is granted or not needed (Android < 13)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required for Android < 13
            true
        }
    }
    
    /**
     * Register for notification permission request
     * 
     * @param activity The activity to register the permission request
     * @param onResult Callback for permission result
     * @return ActivityResultLauncher for requesting permission
     */
    fun registerForNotificationPermission(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
    }
    
    /**
     * Request notification permission if needed
     * 
     * @param context Application context
     * @param permissionLauncher The permission launcher
     * @return true if permission is already granted or not needed
     */
    fun requestNotificationPermissionIfNeeded(
        context: Context,
        permissionLauncher: ActivityResultLauncher<String>
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission(context)) {
                true
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                false
            }
        } else {
            // Permission not required for Android < 13
            true
        }
    }
}
