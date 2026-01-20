package com.agcoding.cartrackingapp.di

import android.content.Context
import com.agcoding.cartrackingapp.data.preferences.OnboardingPreferences
import com.agcoding.cartrackingapp.data.preferences.ReminderBannerPreferences
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideOnboardingPreferences(
        @ApplicationContext context: Context
    ): OnboardingPreferences {
        return OnboardingPreferences(context)
    }

    @Provides
    @Singleton
    fun provideSettingsPreferences(
        @ApplicationContext context: Context
    ): SettingsPreferences {
        return SettingsPreferences(context)
    }

    @Provides
    @Singleton
    fun provideReminderBannerPreferences(
        @ApplicationContext context: Context
    ): ReminderBannerPreferences {
        return ReminderBannerPreferences(context)
    }
}

