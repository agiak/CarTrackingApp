package com.agcoding.cartrackingapp.di

import android.content.Context
import androidx.room.Room
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_10_11
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_11_12
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_12_13
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_13_14
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_14_15
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_15_16
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_16_17
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_1_2
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_2_3
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_3_4
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_4_5
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_5_6
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_6_7
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_7_8
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_8_9
import com.agcoding.cartrackingapp.data.local.database.MIGRATION_9_10
import com.agcoding.cartrackingapp.data.local.database.dao.CarAttachmentDao
import com.agcoding.cartrackingapp.data.local.database.dao.CarDao
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseCategoryDao
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseDao
import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import com.agcoding.cartrackingapp.data.local.database.dao.NotificationHistoryDao
import com.agcoding.cartrackingapp.data.local.database.dao.TripDao
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17)
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

    @Provides
    @Singleton
    fun provideCarAttachmentDao(database: CarDatabase): CarAttachmentDao {
        return database.carAttachmentDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(database: CarDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    @Singleton
    fun provideNotificationHistoryDao(database: CarDatabase): NotificationHistoryDao {
        return database.notificationHistoryDao()
    }
}
