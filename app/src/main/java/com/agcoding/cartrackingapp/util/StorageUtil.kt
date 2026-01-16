package com.agcoding.cartrackingapp.util

import android.content.Context

/**
 * Result of storage space check
 */
sealed class StorageCheckResult {
    object Sufficient : StorageCheckResult()
    data class Insufficient(val availableBytes: Long, val requiredBytes: Long) :
        StorageCheckResult()

    object Unavailable : StorageCheckResult()

    fun toUserMessage(context: Context): String {
        return when (this) {
            is Sufficient -> "Storage space available"
            is Insufficient -> {
                val availableMB = availableBytes / (1024 * 1024)
                val requiredMB = requiredBytes / (1024 * 1024)
                "Insufficient storage space. Available: ${availableMB}MB, Required: ${requiredMB}MB"
            }

            is Unavailable -> "Storage unavailable"
        }
    }
}

/**
 * Utility for checking storage space availability
 */
object StorageUtil {

    private const val SAFETY_MARGIN_BYTES = 10 * 1024 * 1024L // 10MB safety margin

    /**
     * Checks if there's sufficient storage space for an operation
     * @param context Application context
     * @param requiredBytes Number of bytes needed
     * @return StorageCheckResult indicating if space is available
     */
    fun checkStorageSpace(context: Context, requiredBytes: Long): StorageCheckResult {
        val externalDir = context.getExternalFilesDir(null)
        val usableSpace = externalDir?.usableSpace ?: 0L

        return when {
            usableSpace == 0L -> StorageCheckResult.Unavailable
            usableSpace < (requiredBytes + SAFETY_MARGIN_BYTES) ->
                StorageCheckResult.Insufficient(usableSpace, requiredBytes)

            else -> StorageCheckResult.Sufficient
        }
    }

    /**
     * Estimates the size of a JSON export based on data counts
     * @param carCount Number of cars
     * @param refillCount Number of refills
     * @param expenseCount Number of expenses
     * @return Estimated bytes needed
     */
    fun estimateExportSize(carCount: Int, refillCount: Int, expenseCount: Int): Long {
        // Rough estimates based on JSON structure
        val carBytes = carCount * 500L // ~500 bytes per car with all fields
        val refillBytes = refillCount * 300L // ~300 bytes per refill
        val expenseBytes = expenseCount * 200L // ~200 bytes per expense
        val overhead = 1024L // JSON overhead

        return carBytes + refillBytes + expenseBytes + overhead
    }

    /**
     * Gets the cache directory size
     */
    fun getCacheSize(context: Context): Long {
        return context.cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }

    /**
     * Clears the cache directory
     */
    fun clearCache(context: Context): Boolean {
        return try {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdir()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Formats bytes to human-readable string
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
