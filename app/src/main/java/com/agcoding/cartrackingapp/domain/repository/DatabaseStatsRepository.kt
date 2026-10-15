package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.DatabaseStats
import kotlinx.coroutines.flow.Flow

interface DatabaseStatsRepository {

    /** Emits again whenever any of the counted tables changes. */
    fun observeStats(): Flow<DatabaseStats>
}
