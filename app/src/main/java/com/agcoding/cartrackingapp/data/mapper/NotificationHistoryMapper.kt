package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.NotificationHistoryEntity
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.model.NotificationKind

fun NotificationHistoryEntity.toDomain(): NotificationHistoryItem {
    return NotificationHistoryItem(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp,
        // An unrecognised value means a row written by a newer build than this
        // one. Showing it as an ordinary reminder is the harmless reading.
        kind = when (type) {
            NotificationHistoryEntity.TYPE_ANNOUNCEMENT -> NotificationKind.ANNOUNCEMENT
            else -> NotificationKind.REMINDER
        }
    )
}

fun NotificationHistoryItem.toEntity(): NotificationHistoryEntity {
    return NotificationHistoryEntity(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp,
        type = when (kind) {
            NotificationKind.ANNOUNCEMENT -> NotificationHistoryEntity.TYPE_ANNOUNCEMENT
            NotificationKind.REMINDER -> NotificationHistoryEntity.TYPE_REMINDER
        }
    )
}
