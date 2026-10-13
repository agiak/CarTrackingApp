package com.agcoding.cartrackingapp.presentation.cloud

import androidx.annotation.StringRes
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStage

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

@StringRes
fun SyncStage.labelRes(): Int = when (this) {
    SyncStage.CONNECTING -> R.string.cloud_stage_connect
    SyncStage.CHECKING -> R.string.cloud_stage_checking
    SyncStage.CARS -> R.string.cloud_stage_cars
    SyncStage.TRIPS -> R.string.cloud_stage_trips
    SyncStage.REFILLS -> R.string.cloud_stage_refills
    SyncStage.EXPENSES -> R.string.cloud_stage_expenses
    SyncStage.CATEGORIES -> R.string.cloud_stage_categories
    SyncStage.FINISHING -> R.string.cloud_stage_finish
    SyncStage.DONE -> R.string.cloud_stage_done
}

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
