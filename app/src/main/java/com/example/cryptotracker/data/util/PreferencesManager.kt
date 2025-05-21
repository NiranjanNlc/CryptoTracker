package com.example.cryptotracker.data.util

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptotracker.model.CryptoCurrency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility class for managing SharedPreferences operations
 */
class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    /**
     * Save cryptocurrency list to SharedPreferences
     * 
     * @param cryptoList List of cryptocurrencies to cache
     */
    fun saveCryptoList(cryptoList: List<CryptoCurrency>) {
        val jsonString = gson.toJson(cryptoList)
        sharedPreferences.edit().putString(KEY_CRYPTO_LIST, jsonString).apply()
        // Save timestamp of when data was cached
        sharedPreferences.edit().putLong(KEY_LAST_UPDATED, System.currentTimeMillis()).apply()
    }
    
    /**
     * Get cached cryptocurrency list from SharedPreferences
     * 
     * @return List of cached cryptocurrencies or null if no cache exists
     */
    fun getCachedCryptoList(): List<CryptoCurrency>? {
        val jsonString = sharedPreferences.getString(KEY_CRYPTO_LIST, null) ?: return null
        
        val type = object : TypeToken<List<CryptoCurrency>>() {}.type
        return gson.fromJson(jsonString, type)
    }
    
    /**
     * Get timestamp of when data was last cached
     * 
     * @return Timestamp in milliseconds or 0 if no cache exists
     */
    fun getLastUpdatedTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LAST_UPDATED, 0)
    }
    
    /**
     * Check if cached data is available
     * 
     * @return True if cache exists, false otherwise
     */
    fun hasCachedData(): Boolean {
        return sharedPreferences.contains(KEY_CRYPTO_LIST)
    }
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        sharedPreferences.edit().remove(KEY_CRYPTO_LIST).apply()
        sharedPreferences.edit().remove(KEY_LAST_UPDATED).apply()
    }
    
    companion object {
        private const val PREFERENCES_NAME = "crypto_tracker_preferences"
        private const val KEY_CRYPTO_LIST = "crypto_list"
        private const val KEY_LAST_UPDATED = "last_updated"
    }
}
