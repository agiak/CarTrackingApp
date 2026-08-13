package com.agcoding.cartrackingapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.agcoding.cartrackingapp.data.local.database.dao.CarAttachmentDao
import com.agcoding.cartrackingapp.data.local.database.dao.CarDao
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseCategoryDao
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseDao
import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import com.agcoding.cartrackingapp.data.local.database.dao.NotificationHistoryDao
import com.agcoding.cartrackingapp.data.local.database.dao.TripDao
import com.agcoding.cartrackingapp.data.local.database.entity.CarAttachmentEntity
import com.agcoding.cartrackingapp.data.local.database.entity.CarEntity
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseCategoryEntity
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseEntity
import com.agcoding.cartrackingapp.data.local.database.entity.FuelRefillEntity
import com.agcoding.cartrackingapp.data.local.database.entity.NotificationHistoryEntity
import com.agcoding.cartrackingapp.data.local.database.entity.TripEntity

@Database(
    entities = [CarEntity::class, FuelRefillEntity::class, ExpenseEntity::class, ExpenseCategoryEntity::class, CarAttachmentEntity::class, TripEntity::class, NotificationHistoryEntity::class],
    version = 20,
    exportSchema = false
)
abstract class CarDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun fuelRefillDao(): FuelRefillDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun carAttachmentDao(): CarAttachmentDao
    abstract fun tripDao(): TripDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CarDatabase? = null

        fun getInstance(context: Context): CarDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CarDatabase::class.java,
                    "car_tracking_database"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                        MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                        MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20
                    )
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add tyreSize column to cars table
        db.execSQL("ALTER TABLE cars ADD COLUMN tyreSize TEXT DEFAULT NULL")

        // Add licenseExpiration column to cars table
        db.execSQL("ALTER TABLE cars ADD COLUMN licenseExpiration TEXT DEFAULT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add extra info columns to cars table
        db.execSQL("ALTER TABLE cars ADD COLUMN kteoExpirationDate INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE cars ADD COLUMN lastServiceDate INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE cars ADD COLUMN insuranceExpirationDate INTEGER DEFAULT NULL")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create expenses table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                carId INTEGER NOT NULL,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                timestamp INTEGER NOT NULL,
                notes TEXT,
                FOREIGN KEY(carId) REFERENCES cars(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create index on carId for faster queries
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_carId ON expenses(carId)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add Legal & Compliance fields
        db.execSQL("ALTER TABLE cars ADD COLUMN emissionsCardExpirationDate INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE cars ADD COLUMN roadTaxAmount REAL DEFAULT NULL")
        db.execSQL("ALTER TABLE cars ADD COLUMN roadTaxDueDate INTEGER DEFAULT NULL")

        // Add Maintenance History fields
        db.execSQL("ALTER TABLE cars ADD COLUMN lastTireChangeDate INTEGER DEFAULT NULL")

        // Add Tires Information fields
        db.execSQL("ALTER TABLE cars ADD COLUMN tireBrand TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE cars ADD COLUMN tireDimensions TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE cars ADD COLUMN tireInstallationDate INTEGER DEFAULT NULL")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Migration 5 to 6: Ensure tireInstallationDate column exists
        // Check if column exists, if not add it
        try {
            db.execSQL("ALTER TABLE cars ADD COLUMN tireInstallationDate INTEGER DEFAULT NULL")
        } catch (e: Exception) {
            // Column might already exist from MIGRATION_4_5, ignore error
        }
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Migration 6 to 7: Ensure all required columns exist
        // This migration ensures consistency after schema changes
        val columnsToAdd = listOf(
            "emissionsCardExpirationDate INTEGER DEFAULT NULL",
            "roadTaxAmount REAL DEFAULT NULL",
            "roadTaxDueDate INTEGER DEFAULT NULL",
            "lastTireChangeDate INTEGER DEFAULT NULL",
            "tireBrand TEXT DEFAULT NULL",
            "tireDimensions TEXT DEFAULT NULL",
            "tireInstallationDate INTEGER DEFAULT NULL"
        )

        columnsToAdd.forEach { columnDef ->
            try {
                db.execSQL("ALTER TABLE cars ADD COLUMN $columnDef")
            } catch (e: Exception) {
                // Column might already exist, ignore error
            }
        }
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Migration 7 to 8: Rename 'type' column to 'category' in expenses table
        // SQLite doesn't support RENAME COLUMN directly in older versions, so we:
        // 1. Create new table with correct schema
        // 2. Copy data from old table
        // 3. Drop old table
        // 4. Rename new table

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS expenses_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                carId INTEGER NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                timestamp INTEGER NOT NULL,
                notes TEXT,
                FOREIGN KEY(carId) REFERENCES cars(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Copy data, converting SERVICE/OTHER to proper category names
        db.execSQL("""
            INSERT INTO expenses_new (id, carId, category, amount, timestamp, notes)
            SELECT id, carId, 
                CASE 
                    WHEN type = 'SERVICE' THEN 'Service'
                    WHEN type = 'OTHER' THEN 'Other'
                    ELSE type
                END,
                amount, timestamp, notes
            FROM expenses
        """.trimIndent())

        // Drop old table
        db.execSQL("DROP TABLE expenses")

        // Rename new table
        db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")

        // Recreate index
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_carId ON expenses(carId)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create expense_categories table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS expense_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                isCustom INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())

        // Create unique index on name
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_expense_categories_name ON expense_categories(name)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add service reminder fields to expenses table
        db.execSQL("ALTER TABLE expenses ADD COLUMN reminderDate INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE expenses ADD COLUMN reminderMileage INTEGER DEFAULT NULL")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add reminderEnabled field to expenses table (default = true for existing reminders)
        db.execSQL("ALTER TABLE expenses ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add preExpiryNotificationSent field to expenses table (default = false)
        db.execSQL("ALTER TABLE expenses ADD COLUMN preExpiryNotificationSent INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add reminderDismissed field to expenses table (default = false)
        db.execSQL("ALTER TABLE expenses ADD COLUMN reminderDismissed INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add isDefault field to cars table (default = false)
        db.execSQL("ALTER TABLE cars ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create car_attachments table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS car_attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                carId INTEGER NOT NULL,
                fileName TEXT NOT NULL,
                fileType TEXT NOT NULL,
                fileSizeBytes INTEGER NOT NULL,
                dateAdded INTEGER NOT NULL,
                internalPath TEXT NOT NULL,
                FOREIGN KEY(carId) REFERENCES cars(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indices
        db.execSQL("CREATE INDEX IF NOT EXISTS index_car_attachments_carId ON car_attachments(carId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_car_attachments_dateAdded ON car_attachments(dateAdded)")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create trips table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS trips (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                carId INTEGER NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(carId) REFERENCES cars(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indices for trips
        db.execSQL("CREATE INDEX IF NOT EXISTS index_trips_carId ON trips(carId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_trips_createdAt ON trips(createdAt)")

        // Add tripId column to fuel_refills table
        db.execSQL("ALTER TABLE fuel_refills ADD COLUMN tripId INTEGER DEFAULT NULL")

        // Create index for tripId in fuel_refills
        db.execSQL("CREATE INDEX IF NOT EXISTS index_fuel_refills_tripId ON fuel_refills(tripId)")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create notification_history table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())

        // Create index on timestamp for efficient ordering
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_timestamp ON notification_history(timestamp)")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cars ADD COLUMN deletedAt INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE fuel_refills ADD COLUMN deletedAt INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE expenses ADD COLUMN deletedAt INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE trips ADD COLUMN deletedAt INTEGER DEFAULT NULL")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expense_categories ADD COLUMN isQuickPick INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Persisted, user-editable name of the refill location (reverse-geocoded address).
        db.execSQL("ALTER TABLE fuel_refills ADD COLUMN locationName TEXT DEFAULT NULL")
    }
}

