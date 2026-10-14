package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import kotlinx.coroutines.flow.Flow

interface NotificationHistoryRepository {

    fun getAllNotifications(): Flow<List<NotificationHistoryItem>>

    /** The newest [limit] entries, for screens that show a preview rather than the archive. */
    fun getRecentNotifications(limit: Int): Flow<List<NotificationHistoryItem>>

    suspend fun insertNotification(item: NotificationHistoryItem): Long

    suspend fun deleteAll()

    fun getNotificationCount(): Flow<Int>
}

