package com.agcoding.cartrackingapp.di

import com.agcoding.cartrackingapp.data.repository.CarAttachmentRepositoryImpl
import com.agcoding.cartrackingapp.data.repository.CarRepositoryImpl
import com.agcoding.cartrackingapp.data.repository.ExpenseRepositoryImpl
import com.agcoding.cartrackingapp.data.repository.RefillRepositoryImpl
import com.agcoding.cartrackingapp.data.repository.TripRepositoryImpl
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCarRepository(
        carRepositoryImpl: CarRepositoryImpl
    ): CarRepository

    @Binds
    @Singleton
    abstract fun bindRefillRepository(
        refillRepositoryImpl: RefillRepositoryImpl
    ): RefillRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindCarAttachmentRepository(
        carAttachmentRepositoryImpl: CarAttachmentRepositoryImpl
    ): CarAttachmentRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
}
