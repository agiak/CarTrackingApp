package com.agcoding.cartrackingapp.domain.usecase.expense

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case to get the count of pending reminder alerts where at least one condition has been reached.
 * Only evaluates reminders from the deduplicated "Upcoming Services Reminders" list.
 *
 * - Date-based: reminder date has passed or is today
 * - Mileage-based: remaining kilometers <= threshold (500 km)
 */
class GetPendingReminderAlertsCountUseCase @Inject constructor(
    private val getExpensesWithRemindersUseCase: GetExpensesWithRemindersUseCase
) {
    companion object {
        // Mileage threshold for considering a reminder as "reached"
        private const val MILEAGE_THRESHOLD_KM = 500
    }

    operator fun invoke(): Flow<Int> {
        return getExpensesWithRemindersUseCase().map { upcomingReminders ->
            val now = System.currentTimeMillis()

            // Get end of today for proper date comparison
            val endOfDay = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            upcomingReminders.count { reminder ->
                val expense = reminder.expense

                // Only count if:
                // 1. Reminder is enabled
                // 2. Reminder was NOT dismissed
                // 3. At least one condition (date OR mileage) has been REACHED

                if (!expense.reminderEnabled || expense.reminderDismissed) {
                    return@count false
                }

                // Check if reminder date or mileage exists
                if (expense.reminderDate == null && expense.reminderMileage == null) {
                    return@count false
                }

                var conditionReached = false

                // Check date-based reminder - only count if date has passed or is today
                expense.reminderDate?.let { reminderDate ->
                    if (reminderDate <= endOfDay) {
                        conditionReached = true
                    }
                }

                // Check mileage-based reminder - only count if remaining km <= threshold
                expense.reminderMileage?.let { targetMileage ->
                    val currentMileage = reminder.currentOdometer.toInt()
                    val remainingKm = targetMileage - currentMileage

                    // Count if remaining kilometers <= threshold
                    if (remainingKm <= MILEAGE_THRESHOLD_KM) {
                        conditionReached = true
                    }
                }

                conditionReached
            }
        }
    }
}
