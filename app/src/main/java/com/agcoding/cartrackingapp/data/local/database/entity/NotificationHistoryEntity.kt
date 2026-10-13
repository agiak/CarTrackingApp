package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.ColumnInfo
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
    // The literal has to be spelled out: an annotation argument must be a
    // compile-time constant, and it must match both [TYPE_REMINDER] below and
    // the DEFAULT in MIGRATION_21_22, or Room's schema check fails at startup.
    @ColumnInfo(defaultValue = "REMINDER")
    val type: String = TYPE_REMINDER
) {
    companion object {
        const val TYPE_REMINDER = "REMINDER"
        const val TYPE_ANNOUNCEMENT = "ANNOUNCEMENT"
    }
}

