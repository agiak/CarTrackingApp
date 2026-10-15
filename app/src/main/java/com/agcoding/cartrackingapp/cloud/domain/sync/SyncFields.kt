package com.agcoding.cartrackingapp.cloud.domain.sync

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The column names of the synced tables, and the rules for spelling their
 * values the same way on both sides of the wire.
 *
 * This is the single most load-bearing detail of the whole sync. A row's hash
 * is computed from these strings on the phone and again from what PostgREST
 * returns; if the two spellings differ by so much as a trailing zero, the row
 * looks changed on every pass and the app re-uploads it forever. So anything
 * that has more than one valid spelling goes through here, on both sides.
 *
 * The constants are the Postgres column names verbatim, because
 * [com.agcoding.cartrackingapp.cloud.data.remote.RemoteRows] builds and reads
 * rows straight from them rather than from a mapper per table.
 */
object SyncFields {

    object Car {
        const val NAME = "name"
        const val LICENSE_PLATE = "license_plate"
        const val INITIAL_ODOMETER = "initial_odometer"
        const val CURRENT_ODOMETER = "current_odometer"
        const val IS_DEFAULT = "is_default"
        const val INSURANCE_EXPIRATION_DATE = "insurance_expiration_date"
        const val KTEO_EXPIRATION_DATE = "kteo_expiration_date"
        const val EMISSIONS_CARD_EXPIRATION_DATE = "emissions_card_expiration_date"
        const val ROAD_TAX_AMOUNT = "road_tax_amount"
        const val ROAD_TAX_DUE_DATE = "road_tax_due_date"
        const val LAST_SERVICE_DATE = "last_service_date"
        const val LAST_TIRE_CHANGE_DATE = "last_tire_change_date"
        const val TIRE_BRAND = "tire_brand"
        const val TIRE_DIMENSIONS = "tire_dimensions"
        const val TIRE_INSTALLATION_DATE = "tire_installation_date"
        const val TYRE_SIZE = "tyre_size"
        const val LICENSE_EXPIRATION = "license_expiration"
    }

    object Trip {
        const val CAR_ID = "car_id"
        const val NAME = "name"
        const val DESCRIPTION = "description"
    }

    object Refill {
        const val CAR_ID = "car_id"
        const val TRIP_ID = "trip_id"
        const val AMOUNT_PAID = "amount_paid"
        const val LITERS_ADDED = "liters_added"
        const val TRIP_DISTANCE = "trip_distance"
        const val ODOMETER_READING = "odometer_reading"
        const val FUEL_CONSUMPTION = "fuel_consumption"
        const val PRICE_PER_LITER = "price_per_liter"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val LOCATION_NAME = "location_name"
        const val TIMESTAMP = "timestamp"
        const val NOTES = "notes"
    }

    object Expense {
        const val CAR_ID = "car_id"
        const val CATEGORY = "category"
        const val AMOUNT = "amount"
        const val TIMESTAMP = "timestamp"
        const val NOTES = "notes"
        const val REMINDER_DATE = "reminder_date"
        const val REMINDER_MILEAGE = "reminder_mileage"
        const val REMINDER_ENABLED = "reminder_enabled"
        const val PRE_EXPIRY_NOTIFICATION_SENT = "pre_expiry_notification_sent"
        const val REMINDER_DISMISSED = "reminder_dismissed"
    }

    object Category {
        const val IS_CUSTOM = "is_custom"
        const val IS_QUICK_PICK = "is_quick_pick"
    }

    /** Shared by several tables, and the same type in all of them. */
    const val CREATED_AT = "created_at"
    const val UPDATED_AT = "updated_at"

    /**
     * The app's own Trash, as an ordinary synced field: an item the user put in
     * the bin on one phone is in the bin on the other, and still restorable.
     * Not to be confused with the server's `deleted_at`, which means gone for
     * good and is handled by the sync itself rather than carried here.
     */
    const val TRASHED_AT = "trashed_at"

    /**
     * Columns the server stores as `numeric(p,s)` and echoes back at exactly
     * that scale, mapped to the scale so both sides agree on the spelling.
     */
    val DECIMAL_SCALE: Map<String, Int> = mapOf(
        Car.INITIAL_ODOMETER to 2,
        Car.CURRENT_ODOMETER to 2,
        Car.ROAD_TAX_AMOUNT to 2,
        Refill.AMOUNT_PAID to 2,
        Refill.LITERS_ADDED to 3,
        Refill.TRIP_DISTANCE to 2,
        Refill.ODOMETER_READING to 2,
        Refill.FUEL_CONSUMPTION to 3,
        Refill.PRICE_PER_LITER to 3,
        Refill.LATITUDE to 7,
        Refill.LONGITUDE to 7,
        Expense.AMOUNT to 2,
    )

    /**
     * Columns stored as `bigint` or `integer`. Times are here rather than as
     * timestamps on purpose: an integer has exactly one spelling, where a
     * timestamp has several and any of them would break the hash.
     */
    val WHOLE_NUMBER: Set<String> = setOf(
        CREATED_AT,
        UPDATED_AT,
        TRASHED_AT,
        Car.INSURANCE_EXPIRATION_DATE,
        Car.KTEO_EXPIRATION_DATE,
        Car.EMISSIONS_CARD_EXPIRATION_DATE,
        Car.ROAD_TAX_DUE_DATE,
        Car.LAST_SERVICE_DATE,
        Car.LAST_TIRE_CHANGE_DATE,
        Car.TIRE_INSTALLATION_DATE,
        Refill.TIMESTAMP,
        Expense.REMINDER_MILEAGE,
    )

    val BOOLEAN: Set<String> = setOf(
        Car.IS_DEFAULT,
        Expense.REMINDER_ENABLED,
        Expense.PRE_EXPIRY_NOTIFICATION_SENT,
        Expense.REMINDER_DISMISSED,
        Category.IS_CUSTOM,
        Category.IS_QUICK_PICK,
    )

    /** `numeric(p,2)` always comes back with two decimals — so send two. */
    fun money(value: Double): String =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString()

    /** For columns that are not money but still numeric with a fixed scale. */
    fun decimal(value: Double, scale: Int): String =
        BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).toPlainString()

    /** The spelling for a column named in [DECIMAL_SCALE], whatever its scale. */
    fun decimalFor(column: String, value: Double): String =
        decimal(value, DECIMAL_SCALE[column] ?: 2)

    /** As above, but a null stays a null rather than becoming a zero. */
    fun decimalFor(column: String, value: Double?): String? =
        value?.let { decimalFor(column, it) }

    /** Epoch millis to a `date`, in the phone's zone — the day the user saw. */
    fun date(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(
            Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        )

    /** Epoch millis to a `timestamptz`, always in UTC. */
    fun timestamp(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()

    /**
     * A set of ids as one comparable string: blanks dropped, duplicates
     * dropped, sorted. Postgres gives no order guarantee for an array column,
     * so without sorting the same set hashes differently each way round.
     */
    fun idList(ids: Collection<String>): String =
        ids.filter { it.isNotBlank() }.distinct().sorted().joinToString(",")

    /**
     * Text cut to the column's CHECK limit. One row that violates a constraint
     * makes PostgREST reject the entire batch it travelled in, so the trimming
     * has to happen before the upload, not be left to the database.
     */
    fun text(value: String?, maxLength: Int): String? =
        value?.trim()?.takeIf { it.isNotEmpty() }?.take(maxLength)
}
