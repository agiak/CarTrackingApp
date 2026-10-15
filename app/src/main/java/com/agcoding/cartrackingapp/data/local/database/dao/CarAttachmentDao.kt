package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agcoding.cartrackingapp.data.local.database.entity.CarAttachmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for car attachments
 */
@Dao
interface CarAttachmentDao {

    @Query("SELECT * FROM car_attachments WHERE carId = :carId ORDER BY dateAdded DESC")
    fun getAttachmentsForCar(carId: Long): Flow<List<CarAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: CarAttachmentEntity): Long

    @Update
    suspend fun update(attachment: CarAttachmentEntity)

    @Delete
    suspend fun delete(attachment: CarAttachmentEntity)

    @Query("DELETE FROM car_attachments WHERE carId = :carId")
    suspend fun deleteAllForCar(carId: Long)

    @Query("SELECT * FROM car_attachments WHERE id = :id")
    suspend fun getAttachmentById(id: Long): CarAttachmentEntity?

    /**
     * Attachments are never uploaded — they stay on the phone — but they still
     * belong to the cars they hang off, so wiping the account's local data
     * takes them with it.
     */
    @Query("DELETE FROM car_attachments")
    suspend fun deleteAll()
}

