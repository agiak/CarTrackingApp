package com.agcoding.cartrackingapp.di

import android.content.Context
import androidx.room.Room
import com.agcoding.cartrackingapp.cloud.data.local.SyncLinkDao
import com.agcoding.cartrackingapp.data.local.database.ALL_MIGRATIONS
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import com.agcoding.cartrackingapp.data.local.database.dao.CarAttachmentDao
import com.agcoding.cartrackingapp.data.local.database.dao.CarDao
import com.agcoding.cartrackingapp.data.local.database.dao.DatabaseStatsDao
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
            // The shared list, never a copy. This module and the widget's
            // builder both open the same file on disk, so a migration either
            // reaches both of them or the app breaks for whichever one missed
            // it — which is exactly how version 22 shipped broken.
            .addMigrations(*ALL_MIGRATIONS)
            .fallbackToDestructiveMigrationOnDowngrade()
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

    @Provides
    @Singleton
    fun provideSyncLinkDao(database: CarDatabase): SyncLinkDao {
        return database.syncLinkDao()
    }

    @Provides
    @Singleton
    fun provideDatabaseStatsDao(database: CarDatabase): DatabaseStatsDao {
        return database.databaseStatsDao()
    }
}
