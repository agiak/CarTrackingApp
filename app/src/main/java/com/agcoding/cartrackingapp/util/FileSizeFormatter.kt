package com.agcoding.cartrackingapp.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utility to format file sizes in human-readable format
 */
object FileSizeFormatter {

    private val units = arrayOf("B", "KB", "MB", "GB")

    fun format(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        val formattedSize = DecimalFormat("#,##0.#").format(
            bytes / 1024.0.pow(digitGroups.toDouble())
        )

        return "$formattedSize ${units[digitGroups]}"
    }
}

