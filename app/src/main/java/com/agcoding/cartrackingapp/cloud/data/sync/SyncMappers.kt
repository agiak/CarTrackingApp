package com.agcoding.cartrackingapp.cloud.data.sync

import com.agcoding.cartrackingapp.cloud.domain.sync.LocalItem
import com.agcoding.cartrackingapp.cloud.domain.sync.RemoteItem
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields.Car
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields.Category
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields.Expense
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields.Refill
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncFields.Trip
import com.agcoding.cartrackingapp.data.local.database.entity.CarEntity
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseCategoryEntity
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseEntity
import com.agcoding.cartrackingapp.data.local.database.entity.FuelRefillEntity
import com.agcoding.cartrackingapp.data.local.database.entity.TripEntity

/**
 * Room rows to sync fields and back.
 *
 * Two rules run through all of it.
 *
 * **Clamp on the way out.** Every `take`, `coerce` and fallback here mirrors a
 * CHECK constraint in the migration. One row that breaks a constraint makes
 * PostgREST reject the entire 200-row batch it travelled in, so a 400-char
 * note has to be cut here rather than be argued about with the database.
 *
 * **A child names its parent by the parent's *remote* id.** Local ids are this
 * phone's Room counters and mean nothing on another device, so a row whose car
 * has not been synced yet cannot be expressed at all — those map to null and
 * are left for the next round, by which time the car will have an id.
 */

// ── cars ─────────────────────────────────────────────────────────────────

internal fun CarEntity.toLocalItem(): LocalItem = LocalItem(
    localKey = id.toString(),
    fields = mapOf(
        // `not null check (length(trim(name)) between 1 and 100)` — a blank
        // name is possible locally and would fail the whole batch.
        Car.NAME to (SyncFields.text(name, 100) ?: UNNAMED_CAR),
        Car.LICENSE_PLATE to (SyncFields.text(licensePlate, 32) ?: ""),
        Car.INITIAL_ODOMETER to SyncFields.decimalFor(Car.INITIAL_ODOMETER, initialOdometer),
        Car.CURRENT_ODOMETER to SyncFields.decimalFor(Car.CURRENT_ODOMETER, currentOdometer),
        Car.IS_DEFAULT to isDefault.toString(),
        Car.INSURANCE_EXPIRATION_DATE to insuranceExpirationDate?.toString(),
        Car.KTEO_EXPIRATION_DATE to kteoExpirationDate?.toString(),
        Car.EMISSIONS_CARD_EXPIRATION_DATE to emissionsCardExpirationDate?.toString(),
        Car.ROAD_TAX_AMOUNT to SyncFields.decimalFor(Car.ROAD_TAX_AMOUNT, roadTaxAmount),
        Car.ROAD_TAX_DUE_DATE to roadTaxDueDate?.toString(),
        Car.LAST_SERVICE_DATE to lastServiceDate?.toString(),
        Car.LAST_TIRE_CHANGE_DATE to lastTireChangeDate?.toString(),
        Car.TIRE_BRAND to SyncFields.text(tireBrand, 100),
        Car.TIRE_DIMENSIONS to SyncFields.text(tireDimensions, 100),
        Car.TIRE_INSTALLATION_DATE to tireInstallationDate?.toString(),
        Car.TYRE_SIZE to SyncFields.text(tyreSize, 100),
        Car.LICENSE_EXPIRATION to SyncFields.text(licenseExpiration, 100),
        SyncFields.CREATED_AT to createdAt.toString(),
        SyncFields.UPDATED_AT to updatedAt.toString(),
        SyncFields.TRASHED_AT to deletedAt?.toString(),
    ),
)

internal fun RemoteItem.toCar(id: Long): CarEntity = CarEntity(
    id = id,
    name = fields[Car.NAME].orEmpty(),
    licensePlate = fields[Car.LICENSE_PLATE].orEmpty(),
    initialOdometer = fields[Car.INITIAL_ODOMETER].toDoubleOrZero(),
    currentOdometer = fields[Car.CURRENT_ODOMETER].toDoubleOrZero(),
    isDefault = fields[Car.IS_DEFAULT].toBoolean(),
    insuranceExpirationDate = fields[Car.INSURANCE_EXPIRATION_DATE]?.toLongOrNull(),
    kteoExpirationDate = fields[Car.KTEO_EXPIRATION_DATE]?.toLongOrNull(),
    emissionsCardExpirationDate = fields[Car.EMISSIONS_CARD_EXPIRATION_DATE]?.toLongOrNull(),
    roadTaxAmount = fields[Car.ROAD_TAX_AMOUNT]?.toDoubleOrNull(),
    roadTaxDueDate = fields[Car.ROAD_TAX_DUE_DATE]?.toLongOrNull(),
    lastServiceDate = fields[Car.LAST_SERVICE_DATE]?.toLongOrNull(),
    lastTireChangeDate = fields[Car.LAST_TIRE_CHANGE_DATE]?.toLongOrNull(),
    tireBrand = fields[Car.TIRE_BRAND],
    tireDimensions = fields[Car.TIRE_DIMENSIONS],
    tireInstallationDate = fields[Car.TIRE_INSTALLATION_DATE]?.toLongOrNull(),
    tyreSize = fields[Car.TYRE_SIZE],
    licenseExpiration = fields[Car.LICENSE_EXPIRATION],
    deletedAt = fields[SyncFields.TRASHED_AT]?.toLongOrNull(),
    createdAt = fields[SyncFields.CREATED_AT]?.toLongOrNull() ?: 0L,
    updatedAt = fields[SyncFields.UPDATED_AT]?.toLongOrNull() ?: 0L,
)

// ── trips ────────────────────────────────────────────────────────────────

internal fun TripEntity.toLocalItem(carToRemote: Map<Long, String>): LocalItem? {
    val carRemoteId = carToRemote[carId] ?: return null
    return LocalItem(
        localKey = id.toString(),
        fields = mapOf(
            Trip.CAR_ID to carRemoteId,
            Trip.NAME to (SyncFields.text(name, 100) ?: ""),
            Trip.DESCRIPTION to SyncFields.text(description, 500),
            SyncFields.CREATED_AT to createdAt.toString(),
            SyncFields.UPDATED_AT to updatedAt.toString(),
            SyncFields.TRASHED_AT to deletedAt?.toString(),
        ),
    )
}

internal fun RemoteItem.toTrip(id: Long, carToLocal: Map<String, Long>): TripEntity? {
    val carId = carToLocal[fields[Trip.CAR_ID]] ?: return null
    return TripEntity(
        id = id,
        carId = carId,
        name = fields[Trip.NAME].orEmpty(),
        description = fields[Trip.DESCRIPTION],
        createdAt = fields[SyncFields.CREATED_AT]?.toLongOrNull() ?: 0L,
        updatedAt = fields[SyncFields.UPDATED_AT]?.toLongOrNull() ?: 0L,
        deletedAt = fields[SyncFields.TRASHED_AT]?.toLongOrNull(),
    )
}

// ── refills ──────────────────────────────────────────────────────────────

internal fun FuelRefillEntity.toLocalItem(
    carToRemote: Map<Long, String>,
    tripToRemote: Map<Long, String>,
): LocalItem? {
    val carRemoteId = carToRemote[carId] ?: return null
    return LocalItem(
        localKey = id.toString(),
        fields = mapOf(
            Refill.CAR_ID to carRemoteId,
            // A trip that has not synced yet simply means "not grouped" for
            // now; the refill itself must not wait for it.
            Refill.TRIP_ID to tripId?.let { tripToRemote[it] },
            Refill.AMOUNT_PAID to SyncFields.decimalFor(Refill.AMOUNT_PAID, amountPaid),
            Refill.LITERS_ADDED to SyncFields.decimalFor(Refill.LITERS_ADDED, litersAdded),
            Refill.TRIP_DISTANCE to SyncFields.decimalFor(Refill.TRIP_DISTANCE, tripDistance),
            Refill.ODOMETER_READING to SyncFields.decimalFor(Refill.ODOMETER_READING, odometerReading),
            Refill.FUEL_CONSUMPTION to SyncFields.decimalFor(Refill.FUEL_CONSUMPTION, fuelConsumption),
            Refill.PRICE_PER_LITER to SyncFields.decimalFor(Refill.PRICE_PER_LITER, pricePerLiter),
            Refill.LATITUDE to SyncFields.decimalFor(Refill.LATITUDE, latitude),
            Refill.LONGITUDE to SyncFields.decimalFor(Refill.LONGITUDE, longitude),
            Refill.LOCATION_NAME to SyncFields.text(locationName, 200),
            Refill.TIMESTAMP to timestamp.toString(),
            Refill.NOTES to SyncFields.text(notes, 1000),
            SyncFields.TRASHED_AT to deletedAt?.toString(),
        ),
    )
}

internal fun RemoteItem.toRefill(
    id: Long,
    carToLocal: Map<String, Long>,
    tripToLocal: Map<String, Long>,
): FuelRefillEntity? {
    val carId = carToLocal[fields[Refill.CAR_ID]] ?: return null
    return FuelRefillEntity(
        id = id,
        carId = carId,
        tripId = fields[Refill.TRIP_ID]?.let { tripToLocal[it] },
        amountPaid = fields[Refill.AMOUNT_PAID].toDoubleOrZero(),
        litersAdded = fields[Refill.LITERS_ADDED].toDoubleOrZero(),
        tripDistance = fields[Refill.TRIP_DISTANCE].toDoubleOrZero(),
        odometerReading = fields[Refill.ODOMETER_READING].toDoubleOrZero(),
        fuelConsumption = fields[Refill.FUEL_CONSUMPTION].toDoubleOrZero(),
        pricePerLiter = fields[Refill.PRICE_PER_LITER].toDoubleOrZero(),
        latitude = fields[Refill.LATITUDE]?.toDoubleOrNull(),
        longitude = fields[Refill.LONGITUDE]?.toDoubleOrNull(),
        locationName = fields[Refill.LOCATION_NAME],
        timestamp = fields[Refill.TIMESTAMP]?.toLongOrNull() ?: 0L,
        notes = fields[Refill.NOTES],
        deletedAt = fields[SyncFields.TRASHED_AT]?.toLongOrNull(),
    )
}

// ── expenses ─────────────────────────────────────────────────────────────

internal fun ExpenseEntity.toLocalItem(carToRemote: Map<Long, String>): LocalItem? {
    val carRemoteId = carToRemote[carId] ?: return null
    return LocalItem(
        localKey = id.toString(),
        fields = mapOf(
            Expense.CAR_ID to carRemoteId,
            Expense.CATEGORY to (SyncFields.text(category, 100) ?: ""),
            Expense.AMOUNT to SyncFields.decimalFor(Expense.AMOUNT, amount),
            Expense.TIMESTAMP to timestamp.toString(),
            Expense.NOTES to SyncFields.text(notes, 1000),
            Expense.REMINDER_DATE to reminderDate?.toString(),
            Expense.REMINDER_MILEAGE to reminderMileage?.toString(),
            Expense.REMINDER_ENABLED to reminderEnabled.toString(),
            Expense.PRE_EXPIRY_NOTIFICATION_SENT to preExpiryNotificationSent.toString(),
            Expense.REMINDER_DISMISSED to reminderDismissed.toString(),
            SyncFields.TRASHED_AT to deletedAt?.toString(),
        ),
    )
}

internal fun RemoteItem.toExpense(id: Long, carToLocal: Map<String, Long>): ExpenseEntity? {
    val carId = carToLocal[fields[Expense.CAR_ID]] ?: return null
    return ExpenseEntity(
        id = id,
        carId = carId,
        category = fields[Expense.CATEGORY].orEmpty(),
        amount = fields[Expense.AMOUNT].toDoubleOrZero(),
        timestamp = fields[Expense.TIMESTAMP]?.toLongOrNull() ?: 0L,
        notes = fields[Expense.NOTES],
        reminderDate = fields[Expense.REMINDER_DATE]?.toLongOrNull(),
        reminderMileage = fields[Expense.REMINDER_MILEAGE]?.toIntOrNull(),
        reminderEnabled = fields[Expense.REMINDER_ENABLED].toBoolean(),
        preExpiryNotificationSent = fields[Expense.PRE_EXPIRY_NOTIFICATION_SENT].toBoolean(),
        reminderDismissed = fields[Expense.REMINDER_DISMISSED].toBoolean(),
        deletedAt = fields[SyncFields.TRASHED_AT]?.toLongOrNull(),
    )
}

// ── expense categories ───────────────────────────────────────────────────

/** Keyed by name: the same name on two phones is the same category. */
internal fun ExpenseCategoryEntity.toLocalItem(): LocalItem = LocalItem(
    localKey = name,
    fields = mapOf(
        Category.IS_CUSTOM to isCustom.toString(),
        Category.IS_QUICK_PICK to isQuickPick.toString(),
        SyncFields.CREATED_AT to createdAt.toString(),
    ),
)

internal fun RemoteItem.toCategory(id: Long, name: String): ExpenseCategoryEntity =
    ExpenseCategoryEntity(
        id = id,
        name = name,
        isCustom = fields[Category.IS_CUSTOM].toBoolean(),
        isQuickPick = fields[Category.IS_QUICK_PICK].toBoolean(),
        createdAt = fields[SyncFields.CREATED_AT]?.toLongOrNull() ?: 0L,
    )

// ── helpers ──────────────────────────────────────────────────────────────

private const val UNNAMED_CAR = "Car"

private fun String?.toDoubleOrZero(): Double = this?.toDoubleOrNull() ?: 0.0

private fun String?.toBoolean(): Boolean = this == "true"
