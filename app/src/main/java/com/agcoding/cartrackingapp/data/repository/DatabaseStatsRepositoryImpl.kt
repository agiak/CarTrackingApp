package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.DatabaseStatsDao
import com.agcoding.cartrackingapp.domain.model.DatabaseStats
import com.agcoding.cartrackingapp.domain.repository.DatabaseStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DatabaseStatsRepositoryImpl @Inject constructor(
    private val dao: DatabaseStatsDao
) : DatabaseStatsRepository {

    override fun observeStats(): Flow<DatabaseStats> = combine(
        dao.observeCarCount(),
        dao.observeRefillCount(),
        dao.observeExpenseCount(),
        dao.observeTripCount(),
    ) { cars, refills, expenses, trips ->
        DatabaseStats(
            cars = cars,
            refills = refills,
            expenses = expenses,
            trips = trips,
        )
    }
}
