package com.agcoding.cartrackingapp.presentation.cloud

import androidx.annotation.StringRes
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStage
import com.agcoding.cartrackingapp.presentation.cloud.sync.CloudSyncDirection

/**
 * The one place a [CloudError] becomes something a person can read.
 *
 * Exhaustive on purpose — no `else` branch — so adding a case to the enum
 * stops compiling here instead of silently reaching the user as "something
 * went wrong".
 */
@StringRes
fun CloudError.messageRes(): Int = when (this) {
    CloudError.INVALID_CREDENTIALS -> R.string.cloud_error_invalid_credentials
    CloudError.USER_EXISTS -> R.string.cloud_error_user_exists
    CloudError.WEAK_PASSWORD -> R.string.cloud_error_weak_password
    CloudError.SAME_PASSWORD -> R.string.cloud_error_same_password
    CloudError.EMAIL_SEND_FAILED -> R.string.cloud_error_email_send_failed
    CloudError.INVALID_EMAIL -> R.string.cloud_error_invalid_email
    CloudError.RATE_LIMITED -> R.string.cloud_error_rate_limited
    CloudError.SESSION_EXPIRED -> R.string.cloud_error_session_expired
    CloudError.NO_CONNECTION -> R.string.cloud_error_no_connection
    CloudError.UNSYNCED_CHANGES -> R.string.cloud_error_unsynced_changes
    CloudError.UNKNOWN -> R.string.cloud_error_unknown
}

/**
 * What the bar is doing right now, in the direction it is doing it.
 *
 * The same five stages read as the opposite of the truth when the data is
 * coming the other way: someone who has just signed in to get their records
 * back is not waiting for them to be uploaded. Connecting, checking and
 * finishing say nothing about direction, so they are shared.
 */
@StringRes
fun SyncStage.labelRes(direction: CloudSyncDirection): Int = when (this) {
    SyncStage.CONNECTING -> R.string.cloud_stage_connect
    SyncStage.CHECKING -> R.string.cloud_stage_checking
    SyncStage.CARS -> direction.pick(R.string.cloud_stage_cars, R.string.cloud_stage_cars_down)
    SyncStage.TRIPS -> direction.pick(R.string.cloud_stage_trips, R.string.cloud_stage_trips_down)
    SyncStage.REFILLS -> direction.pick(R.string.cloud_stage_refills, R.string.cloud_stage_refills_down)
    SyncStage.EXPENSES -> direction.pick(R.string.cloud_stage_expenses, R.string.cloud_stage_expenses_down)
    SyncStage.CATEGORIES -> direction.pick(
        R.string.cloud_stage_categories,
        R.string.cloud_stage_categories_down,
    )
    SyncStage.FINISHING -> R.string.cloud_stage_finish
    SyncStage.DONE -> R.string.cloud_stage_done
}

@StringRes
private fun CloudSyncDirection.pick(@StringRes up: Int, @StringRes down: Int): Int =
    if (this == CloudSyncDirection.Download) down else up

/** The little picture above the stage label on the progress screen. */
val SyncStage.emoji: String
    get() = when (this) {
        SyncStage.CONNECTING -> "☁️"
        SyncStage.CHECKING -> "🔍"
        SyncStage.CARS -> "🚗"
        SyncStage.TRIPS -> "🗺️"
        SyncStage.REFILLS -> "⛽"
        SyncStage.EXPENSES -> "🧾"
        SyncStage.CATEGORIES -> "🏷️"
        SyncStage.FINISHING -> "✨"
        SyncStage.DONE -> "✅"
    }
