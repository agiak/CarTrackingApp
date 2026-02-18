package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for car attachments
 */
@Entity(
    tableName = "car_attachments",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carId"), Index("dateAdded")]
)
data class CarAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long,
    val fileName: String,
    val fileType: String, // "PDF" or "IMAGE"
    val fileSizeBytes: Long,
    val dateAdded: Long,
    val internalPath: String // Relative path within app storage
)

