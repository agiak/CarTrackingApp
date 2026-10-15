package com.agcoding.cartrackingapp.cloud.data.remote

import com.agcoding.cartrackingapp.cloud.domain.sync.RemoteItem
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields
import org.json.JSONObject
import java.math.BigDecimal

/**
 * The synced tables: their name, the column that identifies a row, and which
 * columns travel.
 *
 * The order of the entries is the order they sync in, and it is a dependency
 * order: a trip points at a car, a refill at a car and a trip, an expense at a
 * car. A child cannot be uploaded before the row it references exists, and the
 * client cannot even name that row until the parent's sync has handed out its
 * id, so this order is load-bearing rather than cosmetic.
 */
enum class RemoteTable(
    val tableName: String,
    /** Column that identifies a row within one user's data. */
    val keyColumn: String,
    val fieldNames: List<String>,
    /** The column a `select` is ordered by, so paging is stable. */
    val orderBy: String = "id.asc",
) {
    CARS(
        tableName = "cars",
        keyColumn = "id",
        fieldNames = listOf(
            SyncFields.Car.NAME,
            SyncFields.Car.LICENSE_PLATE,
            SyncFields.Car.INITIAL_ODOMETER,
            SyncFields.Car.CURRENT_ODOMETER,
            SyncFields.Car.IS_DEFAULT,
            SyncFields.Car.INSURANCE_EXPIRATION_DATE,
            SyncFields.Car.KTEO_EXPIRATION_DATE,
            SyncFields.Car.EMISSIONS_CARD_EXPIRATION_DATE,
            SyncFields.Car.ROAD_TAX_AMOUNT,
            SyncFields.Car.ROAD_TAX_DUE_DATE,
            SyncFields.Car.LAST_SERVICE_DATE,
            SyncFields.Car.LAST_TIRE_CHANGE_DATE,
            SyncFields.Car.TIRE_BRAND,
            SyncFields.Car.TIRE_DIMENSIONS,
            SyncFields.Car.TIRE_INSTALLATION_DATE,
            SyncFields.Car.TYRE_SIZE,
            SyncFields.Car.LICENSE_EXPIRATION,
            SyncFields.CREATED_AT,
            SyncFields.UPDATED_AT,
            SyncFields.TRASHED_AT,
        ),
    ),

    TRIPS(
        tableName = "trips",
        keyColumn = "id",
        fieldNames = listOf(
            SyncFields.Trip.CAR_ID,
            SyncFields.Trip.NAME,
            SyncFields.Trip.DESCRIPTION,
            SyncFields.CREATED_AT,
            SyncFields.UPDATED_AT,
            SyncFields.TRASHED_AT,
        ),
    ),

    REFILLS(
        tableName = "fuel_refills",
        keyColumn = "id",
        fieldNames = listOf(
            SyncFields.Refill.CAR_ID,
            SyncFields.Refill.TRIP_ID,
            SyncFields.Refill.AMOUNT_PAID,
            SyncFields.Refill.LITERS_ADDED,
            SyncFields.Refill.TRIP_DISTANCE,
            SyncFields.Refill.ODOMETER_READING,
            SyncFields.Refill.FUEL_CONSUMPTION,
            SyncFields.Refill.PRICE_PER_LITER,
            SyncFields.Refill.LATITUDE,
            SyncFields.Refill.LONGITUDE,
            SyncFields.Refill.LOCATION_NAME,
            SyncFields.Refill.TIMESTAMP,
            SyncFields.Refill.NOTES,
            SyncFields.TRASHED_AT,
        ),
    ),

    EXPENSES(
        tableName = "expenses",
        keyColumn = "id",
        fieldNames = listOf(
            SyncFields.Expense.CAR_ID,
            SyncFields.Expense.CATEGORY,
            SyncFields.Expense.AMOUNT,
            SyncFields.Expense.TIMESTAMP,
            SyncFields.Expense.NOTES,
            SyncFields.Expense.REMINDER_DATE,
            SyncFields.Expense.REMINDER_MILEAGE,
            SyncFields.Expense.REMINDER_ENABLED,
            SyncFields.Expense.PRE_EXPIRY_NOTIFICATION_SENT,
            SyncFields.Expense.REMINDER_DISMISSED,
            SyncFields.TRASHED_AT,
        ),
    ),

    /**
     * Keyed by its name. Two phones that both have a "Πλύσιμο" category mean
     * the same category, and handing each one a generated id would quietly
     * turn it into two.
     */
    CATEGORIES(
        tableName = "expense_categories",
        keyColumn = "name",
        fieldNames = listOf(
            SyncFields.Category.IS_CUSTOM,
            SyncFields.Category.IS_QUICK_PICK,
            SyncFields.CREATED_AT,
        ),
        orderBy = "name.asc",
    );

    /** What an upsert matches on: the id, or the natural key within the user. */
    val conflictColumns: String
        get() = if (keyColumn == "id") "id" else "user_id,$keyColumn"
}

/**
 * Rows in and out of PostgREST.
 *
 * There is one generic pair here rather than a parser and a serialiser per
 * table: the column list on [RemoteTable] plus the type sets on [SyncFields]
 * say everything the conversion needs, so adding a table is one enum entry and
 * the handful of DAO lambdas in SyncManager — not another two mappers to keep
 * in step with the schema.
 */
object RemoteRows {

    fun parse(table: RemoteTable, row: JSONObject): RemoteItem = RemoteItem(
        remoteId = row.getString(table.keyColumn),
        fields = table.fieldNames.associateWith { name -> readField(row, name) },
        deleted = !row.isNull("deleted_at"),
    )

    /**
     * A full row ready to be upserted, and live: `deleted_at` is cleared on
     * every push, so a row edited here after being deleted elsewhere comes
     * back rather than staying a tombstone.
     */
    fun toJson(
        table: RemoteTable,
        userId: String,
        remoteId: String,
        fields: Map<String, String?>,
    ): JSONObject {
        val row = JSONObject()
            .put(table.keyColumn, remoteId)
            .put("user_id", userId)
            .put("deleted_at", JSONObject.NULL)
        table.fieldNames.forEach { name ->
            val value = fields[name]
            row.put(
                name,
                when {
                    value == null -> JSONObject.NULL
                    name in SyncFields.DECIMAL_SCALE -> BigDecimal(value)
                    name in SyncFields.WHOLE_NUMBER -> value.toLong()
                    name in SyncFields.BOOLEAN -> value.toBoolean()
                    else -> value
                }
            )
        }
        return row
    }

    /**
     * Reads one column back into the exact string the phone would have sent
     * for it. Anything that does not round-trip to the same text makes the row
     * look edited on every sync.
     */
    private fun readField(row: JSONObject, name: String): String? {
        if (row.isNull(name)) return null
        val raw = row.get(name).toString()
        return when {
            name in SyncFields.DECIMAL_SCALE ->
                BigDecimal(raw).setScale(SyncFields.DECIMAL_SCALE.getValue(name)).toPlainString()
            // PostgREST may hand a bigint back as 1.76E+12 depending on the
            // JSON parser's mood; going through BigDecimal pins it to digits.
            name in SyncFields.WHOLE_NUMBER -> BigDecimal(raw).toBigInteger().toString()
            name in SyncFields.BOOLEAN -> raw.toBoolean().toString()
            else -> raw
        }
    }
}
