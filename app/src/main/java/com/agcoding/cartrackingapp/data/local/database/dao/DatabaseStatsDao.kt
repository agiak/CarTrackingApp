package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * How much is actually in the database.
 *
 * Every count excludes soft-deleted rows, because the number has to agree
 * with what the user can see in the app — a stat that counts rows they
 * deleted is worse than no stat at all.
 *
 * Four separate single-table queries rather than one query with four
 * subselects: Room works out which tables to watch by parsing the SQL, and a
 * query it cannot attribute to a table is a Flow that never emits again after
 * the first value.
 */
@Dao
interface DatabaseStatsDao {

    @Query("SELECT COUNT(*) FROM cars WHERE deletedAt IS NULL")
    fun observeCarCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM fuel_refills WHERE deletedAt IS NULL")
    fun observeRefillCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM expenses WHERE deletedAt IS NULL")
    fun observeExpenseCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trips WHERE deletedAt IS NULL")
    fun observeTripCount(): Flow<Int>
}
