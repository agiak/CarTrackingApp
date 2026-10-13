package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {

    @Query("SELECT * FROM expense_categories ORDER BY isCustom ASC, name ASC")
    fun getAllCategories(): Flow<List<ExpenseCategoryEntity>>

    @Query("SELECT * FROM expense_categories WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomCategories(): Flow<List<ExpenseCategoryEntity>>

    @Query("SELECT * FROM expense_categories WHERE isQuickPick = 1 ORDER BY name ASC")
    fun getQuickPickCategories(): Flow<List<ExpenseCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: ExpenseCategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: ExpenseCategoryEntity)

    @Query("DELETE FROM expense_categories WHERE name = :name AND isCustom = 1")
    suspend fun deleteCategoryByName(name: String)

    @Query("SELECT EXISTS(SELECT 1 FROM expense_categories WHERE name = :name LIMIT 1)")
    suspend fun categoryExists(name: String): Boolean

    @Query("UPDATE expense_categories SET isQuickPick = :isQuickPick WHERE name = :name")
    suspend fun setQuickPick(name: String, isQuickPick: Boolean)

    // Sync
    //
    // This table has no `deletedAt`: a category is either there or it is not.

    @Update
    suspend fun updateCategory(category: ExpenseCategoryEntity)

    /**
     * Deletes whatever the id names, custom or not, unlike
     * [deleteCategoryByName] which protects the built-in categories. The sync
     * only ever calls this for a row the server says is gone.
     */
    @Query("DELETE FROM expense_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("SELECT * FROM expense_categories ORDER BY id ASC")
    suspend fun getAllForSync(): List<ExpenseCategoryEntity>

    @Query("DELETE FROM expense_categories")
    suspend fun deleteAll()
}

