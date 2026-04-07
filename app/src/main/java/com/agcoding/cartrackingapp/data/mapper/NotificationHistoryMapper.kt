package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.NotificationHistoryEntity
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem

fun NotificationHistoryEntity.toDomain(): NotificationHistoryItem {
    return NotificationHistoryItem(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp
    )
}

fun NotificationHistoryItem.toEntity(): NotificationHistoryEntity {
    return NotificationHistoryEntity(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp
    )
}

