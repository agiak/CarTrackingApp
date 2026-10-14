package com.agcoding.cartrackingapp.cloud.di

import com.agcoding.cartrackingapp.cloud.data.repository.CloudRepositoryImpl
import com.agcoding.cartrackingapp.cloud.data.sync.SyncManager
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The cloud package's own bindings, kept here rather than in `di/` so the
 * whole feature is one directory that could be deleted in one go.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CloudModule {

    @Binds
    @Singleton
    abstract fun bindCloudRepository(impl: CloudRepositoryImpl): CloudRepository

    @Binds
    @Singleton
    abstract fun bindCloudSync(impl: SyncManager): CloudSync
}
