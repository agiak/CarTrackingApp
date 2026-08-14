package com.agcoding.cartrackingapp.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Centralized, locale-independent number & currency formatting for the app.
 *
 * All values are rendered in the European ("1.000.000,53") style:
 *   - '.' as the thousands (grouping) separator
 *   - ',' as the decimal separator
 *
 * Currency amounts are rendered with a trailing euro sign, e.g. "1.000,53 €".
 *
 * This is intentionally independent of the device Locale so the format is
 * consistent everywhere in the app regardless of system settings.
 */
object AppNumberFormat {

    /** Fixed grouping/decimal symbols: '.' thousands, ',' decimals. */
    private val symbols: DecimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private fun formatter(minDecimals: Int, maxDecimals: Int, grouping: Boolean = true): DecimalFormat =
        DecimalFormat().apply {
            decimalFormatSymbols = symbols
            isGroupingUsed = grouping
            minimumFractionDigits = minDecimals
            maximumFractionDigits = maxDecimals
            roundingMode = RoundingMode.HALF_UP
        }

    /** Number with grouping and a fixed number of decimals, e.g. decimal(1000000.53, 2) -> "1.000.000,53". */
    fun decimal(value: Double, decimals: Int = 2): String =
        formatter(decimals, decimals).format(value)

    /** Number with grouping and up to [maxDecimals] decimals (trailing zeros trimmed), e.g. "1.000,5". */
    fun decimalUpTo(value: Double, maxDecimals: Int = 2): String =
        formatter(0, maxDecimals).format(value)

    /** Whole number with grouping, e.g. integer(1000000) -> "1.000.000". */
    fun integer(value: Long): String = formatter(0, 0).format(value)
    fun integer(value: Int): String = integer(value.toLong())

    /** Money with trailing euro sign, e.g. money(1000.53) -> "1.000,53 €". */
    fun money(value: Double, decimals: Int = 2): String = decimal(value, decimals) + " €"

    /** Money rate per unit, e.g. moneyPer(1.5, "km") -> "1,50 €/km". */
    fun moneyPer(value: Double, unit: String, decimals: Int = 2): String =
        decimal(value, decimals) + " €/" + unit
}

// ---------------------------------------------------------------------------
// Ergonomic extensions
// ---------------------------------------------------------------------------

/** European-formatted number with grouping and fixed [decimals]. */
fun Double.formatNumber(decimals: Int = 2): String = AppNumberFormat.decimal(this, decimals)

/** European-formatted number with grouping and up to [maxDecimals] decimals. */
fun Double.formatNumberUpTo(maxDecimals: Int = 2): String = AppNumberFormat.decimalUpTo(this, maxDecimals)

/** Money string with trailing euro sign, e.g. "1.000,53 €". */
fun Double.formatMoney(decimals: Int = 2): String = AppNumberFormat.money(this, decimals)

/** Money rate string, e.g. "1,50 €/km". */
fun Double.formatMoneyPer(unit: String, decimals: Int = 2): String = AppNumberFormat.moneyPer(this, unit, decimals)

/** European-formatted whole number with grouping. */
fun Int.formatNumber(): String = AppNumberFormat.integer(this)
fun Long.formatNumber(): String = AppNumberFormat.integer(this)

// ---------------------------------------------------------------------------
// Parsing of user-entered values
// ---------------------------------------------------------------------------

/**
 * Parses a user-entered numeric string that may use European grouping/decimals
 * ("1.234,56"), plain US style ("1234.56"), a bare decimal comma ("1234,56"),
 * an optional euro sign and surrounding whitespace. Returns null if not a number.
 *
 * Heuristic: whichever of '.' or ',' appears LAST is treated as the decimal
 * separator; the other is treated as a grouping separator and removed.
 */
fun String.parseLocalizedDouble(): Double? {
    if (isBlank()) return null
    var s = trim().replace("€", "").replace(" ", "").replace(" ", "")
    if (s.isEmpty()) return null

    val lastComma = s.lastIndexOf(',')
    val lastDot = s.lastIndexOf('.')

    s = when {
        lastComma >= 0 && lastDot >= 0 -> {
            // Both present: the later one is the decimal separator.
            if (lastComma > lastDot) {
                s.replace(".", "").replace(',', '.')      // "1.234,56" -> "1234.56"
            } else {
                s.replace(",", "")                          // "1,234.56" -> "1234.56"
            }
        }
        lastComma >= 0 -> s.replace(',', '.')               // "1234,56" -> "1234.56"
        else -> s                                            // "1234.56" or "1234"
    }
    return s.toDoubleOrNull()
}

/** Convenience for integer inputs (odometer, mileage). Truncates any decimals. */
fun String.parseLocalizedLong(): Long? = parseLocalizedDouble()?.toLong()
fun String.parseLocalizedInt(): Int? = parseLocalizedDouble()?.toInt()

// ---------------------------------------------------------------------------
// Sanitizers for numeric TextField input (raw, un-grouped state)
// ---------------------------------------------------------------------------

/**
 * Sanitizes raw text entered into a decimal numeric field. Keeps only digits and
 * a single ',' decimal separator (grouping is applied only visually). Any '.' the
 * user types is treated as a decimal separator. The result never contains grouping
 * separators, so it can be parsed with [parseLocalizedDouble] and grouped for
 * display with ThousandsSeparatorTransformation.
 */
fun sanitizeDecimalInput(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
    var lastSep = -1
    for (i in filtered.indices) if (filtered[i] == '.' || filtered[i] == ',') lastSep = i
    if (lastSep < 0) return filtered
    val intDigits = filtered.substring(0, lastSep).filter { it.isDigit() }
    val decDigits = filtered.substring(lastSep + 1).filter { it.isDigit() }
    return "$intDigits,$decDigits"
}

/** Sanitizes raw text entered into a whole-number field: digits only. */
fun sanitizeIntInput(input: String): String = input.filter { it.isDigit() }

/**
 * Renders a [Double] as the RAW value of a decimal input field: digits plus an
 * optional single ',' decimal separator, and NO grouping separators (grouping is
 * applied visually by ThousandsSeparatorTransformation).
 *
 * Trailing zero decimals are dropped, so a whole number stays whole: 38.0 -> "38",
 * while a genuine decimal is preserved: 38.5 -> "38,5". This matters for values
 * filled in programmatically (e.g. voice input), where a spurious "38,0" both
 * looks wrong and implies a precision the user never spoke.
 *
 * Grouping MUST stay off here: sanitizeDecimalInput treats '.' as a decimal
 * separator, so a grouped "1.234" would be read back as 1,234.
 */
fun Double.formatForDecimalInput(maxDecimals: Int = 2): String =
    DecimalFormat().apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT).apply {
            decimalSeparator = ','
        }
        isGroupingUsed = false
        minimumFractionDigits = 0
        maximumFractionDigits = maxDecimals
        roundingMode = RoundingMode.HALF_UP
    }.format(this)
