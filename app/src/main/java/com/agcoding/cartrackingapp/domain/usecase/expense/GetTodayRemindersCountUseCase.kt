package com.agcoding.cartrackingapp.domain.usecase.expense

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

data class ReminderInfo(
    val dateBasedCount: Int,
    val mileageBasedCount: Int
) {
    val totalCount: Int get() = dateBasedCount + mileageBasedCount
}

/**
 * Use case to get reminder info based on deduplicated upcoming reminders.
 * Only evaluates reminders from the "Upcoming Services Reminders" list where
 * older reminders are overridden by newer ones per service type.
 *
 * - Date-based: reminder date is today
 * - Mileage-based: remaining kilometers <= threshold (500 km)
 */
class GetTodayRemindersCountUseCase @Inject constructor(
    private val getExpensesWithRemindersUseCase: GetExpensesWithRemindersUseCase
) {
    companion object {
        // Mileage threshold for considering a reminder as "reached"
        private const val MILEAGE_THRESHOLD_KM = 500
        private const val TAG = "TodayRemindersUseCase"
    }

    operator fun invoke(): Flow<ReminderInfo> {
        return getExpensesWithRemindersUseCase().map { upcomingReminders ->
            val now = System.currentTimeMillis()

            // Get start and end of today
            val startOfDay = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            var dateBasedCount = 0
            var mileageBasedCount = 0

            Log.d(TAG, "Evaluating ${upcomingReminders.size} deduplicated reminders")

            // Evaluate only the deduplicated upcoming reminders
            upcomingReminders.forEach { reminder ->
                val expense = reminder.expense

                if (!expense.reminderEnabled || expense.reminderDismissed) {
                    Log.d(TAG, "Skipping ${expense.category}: disabled or dismissed")
                    return@forEach
                }

                var dateConditionMet = false
                var mileageConditionMet = false

                // Check date-based reminder - only count if date is today
                expense.reminderDate?.let { reminderDate ->
                    if (reminderDate in startOfDay..endOfDay) {
                        Log.d(TAG, "✓ Date condition met for ${expense.category}")
                        dateConditionMet = true
                    }
                }

                // Check mileage-based reminder - only count if remaining km <= threshold
                expense.reminderMileage?.let { targetMileage ->
                    val currentMileage = reminder.currentOdometer.toInt()
                    val remainingKm = targetMileage - currentMileage

                    Log.d(TAG, "Evaluating ${expense.category}: " +
                            "target=$targetMileage, current=$currentMileage, remaining=$remainingKm")

                    // Count if remaining kilometers <= threshold
                    if (remainingKm <= MILEAGE_THRESHOLD_KM) {
                        Log.d(TAG, "✓ Mileage condition met for ${expense.category}")
                        mileageConditionMet = true
                    } else {
                        Log.d(TAG, "✗ Mileage condition NOT met for ${expense.category} (still ${remainingKm}km away)")
                    }
                }

                // Increment counters based on which conditions were met
                // A reminder counts only once even if both conditions are met
                if (dateConditionMet && !mileageConditionMet) {
                    dateBasedCount++
                } else if (mileageConditionMet && !dateConditionMet) {
                    mileageBasedCount++
                } else if (dateConditionMet && mileageConditionMet) {
                    // If both conditions are met, count it as date-based (higher priority)
                    dateBasedCount++
                }
            }

            Log.d(TAG, "Result: $dateBasedCount date-based, $mileageBasedCount mileage-based")
            ReminderInfo(dateBasedCount, mileageBasedCount)
        }
    }
}
