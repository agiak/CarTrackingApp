package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_history",
    indices = [Index("timestamp")]
)
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    /**
     * Stored as text rather than an enum ordinal so that reordering the enum
     * later cannot silently relabel rows already on disk. Rows written before
     * this column existed read back as `REMINDER`, which is what they were.
     */
    // Deliberately carries no @ColumnInfo(defaultValue = …). Room treats that
    // annotation's value as raw SQL, so a text default would have to be
    // written with its own quotes inside the string — and getting that subtly
    // wrong makes Room reject the schema at startup, which the user sees as
    // "Update Required" and a dead app. With no default declared here, Room
    // skips the comparison entirely; the DEFAULT in MIGRATION_21_22 is only
    // there to fill the column for rows that already existed, and every
    // insert from this app goes through the Kotlin default below.
    val type: String = TYPE_REMINDER
) {
    companion object {
        const val TYPE_REMINDER = "REMINDER"
        const val TYPE_ANNOUNCEMENT = "ANNOUNCEMENT"
    }
}

