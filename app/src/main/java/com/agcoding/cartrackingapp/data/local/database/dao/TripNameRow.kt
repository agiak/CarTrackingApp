package com.agcoding.cartrackingapp.data.local.database.dao

/** A trip reduced to what a label needs: see [TripDao.observeTripNames]. */
data class TripNameRow(
    val id: Long,
    val name: String,
)
