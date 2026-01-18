package com.agcoding.cartrackingapp.di

import android.content.Context
import androidx.room.Room
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_1_2
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_2_3
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_3_4
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_4_5
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_5_6
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_6_7
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_7_8
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_8_9
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_9_10
import com.agcoding.cartrackingapp.data.local.database.dao.CarDao
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseDao
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseCategoryDao
import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCarDatabase(
        @ApplicationContext context: Context
    ): CarDatabase {
        return Room.databaseBuilder(
            context,
            CarDatabase::class.java,
            "car_tracking_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCarDao(database: CarDatabase): CarDao {
        return database.carDao()
    }

    @Provides
    @Singleton
    fun provideFuelRefillDao(database: CarDatabase): FuelRefillDao {
        return database.fuelRefillDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: CarDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideExpenseCategoryDao(database: CarDatabase): ExpenseCategoryDao {
        return database.expenseCategoryDao()
    }
}
