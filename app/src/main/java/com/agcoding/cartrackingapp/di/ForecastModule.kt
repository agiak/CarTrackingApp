package com.agcoding.cartrackingapp.di

import com.agcoding.cartrackingapp.domain.forecast.HoltLinearSmoothingEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing forecast-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ForecastModule {

    /**
     * Provides singleton instance of HoltLinearSmoothingEngine.
     *
     * Default parameters:
     * - alpha = 0.4 (level smoothing)
     * - beta = 0.2 (trend smoothing)
     *
     * These parameters are tuned for car expense data with monthly granularity.
     */
    @Provides
    @Singleton
    fun provideHoltLinearSmoothingEngine(): HoltLinearSmoothingEngine {
        return HoltLinearSmoothingEngine(
            alpha = 0.4,
            beta = 0.2
        )
    }
}

