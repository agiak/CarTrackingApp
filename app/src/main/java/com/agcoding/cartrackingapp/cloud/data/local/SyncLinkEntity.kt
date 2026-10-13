package com.agcoding.cartrackingapp.cloud.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The bridge between a local row and its row on the server.
 *
 * This is the one table the sync adds. Nothing is bolted onto the app's own
 * entities — no `remoteId` column, no `syncedAt`, no migrations through every
 * existing table — so the offline app is untouched and the sync sits on top of
 * it and can be removed again as cleanly as it was added.
 *
 * @param entity the [com.agcoding.cartrackingapp.cloud.data.remote.RemoteTable]
 *   name, so one table holds the links for all of them.
 * @param localKey the Room id as text, or the natural key for tables that have
 *   one. Text because those two are not the same type.
 * @param hash what the row's fields hashed to at the last confirmed sync. An
 *   empty string means "pushed but not confirmed" — see below.
 */
@Entity(tableName = "sync_links", primaryKeys = ["entity", "localKey"])
data class SyncLinkEntity(
    val entity: String,
    val localKey: String,
    val remoteId: String,
    val hash: String,
)

@Dao
interface SyncLinkDao {

    @Query("SELECT * FROM sync_links WHERE entity = :entity")
    suspend fun getAll(entity: String): List<SyncLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(links: List<SyncLinkEntity>)

    @Query("DELETE FROM sync_links WHERE entity = :entity")
    suspend fun deleteAll(entity: String)

    /** Used when signing out, and when a different account signs in. */
    @Query("DELETE FROM sync_links")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM sync_links")
    suspend fun count(): Int
}
