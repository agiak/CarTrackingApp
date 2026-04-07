package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.NotificationHistoryDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.repository.NotificationHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationHistoryRepositoryImpl @Inject constructor(
    private val dao: NotificationHistoryDao
) : NotificationHistoryRepository {

    override fun getAllNotifications(): Flow<List<NotificationHistoryItem>> {
        return dao.getAllNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertNotification(item: NotificationHistoryItem): Long {
        return dao.insertNotification(item.toEntity())
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override fun getNotificationCount(): Flow<Int> {
        return dao.getNotificationCount()
    }
}

