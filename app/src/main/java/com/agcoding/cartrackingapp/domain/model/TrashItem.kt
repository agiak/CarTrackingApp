package com.agcoding.cartrackingapp.domain.model

sealed class TrashItem(val deletedAt: Long) {
    data class CarItem(val car: Car, val deletedAtMs: Long) : TrashItem(deletedAtMs)
    data class RefillItem(val refill: FuelRefill, val carName: String, val deletedAtMs: Long) : TrashItem(deletedAtMs)
    data class ExpenseItem(val expense: Expense, val carName: String, val deletedAtMs: Long) : TrashItem(deletedAtMs)
    data class TripItem(val trip: Trip, val carName: String, val deletedAtMs: Long) : TrashItem(deletedAtMs)
}
