package com.agcoding.cartrackingapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "reminder_check_worker"
        private const val PRE_EXPIRY_DAYS = 1L
        private const val PRE_EXPIRY_KM = 500
        private const val TAG = "ReminderCheckWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "=== ReminderCheckWorker starting ===")

            if (!notificationHelper.areNotificationsEnabled()) {
                Log.w(TAG, "Notifications are disabled by the user or system. " +
                        "Skipping this run – will retry on next schedule.")
                return Result.success() // don't retry endlessly; next periodic run will check again
            }

            checkAndNotifyReminders()
            Log.d(TAG, "=== ReminderCheckWorker completed ===")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during reminder check", e)
            Result.retry() // transient failure → retry
        }
    }

    /**
     * Checks all expenses with reminders and sends notifications for those
     * that are approaching or have passed their reminder thresholds.
     *
     * **Date-based reminders:** Triggers if the reminder date is within
     * [PRE_EXPIRY_DAYS] days from now, or if it is already overdue (past).
     *
     * **Mileage-based reminders:** Triggers if the remaining distance to
     * the target mileage is within [PRE_EXPIRY_KM] km, or already exceeded.
     *
     * Once a notification is sent for an expense the [Expense.preExpiryNotificationSent]
     * flag is set to `true` so the same expense is not re-notified.
     */
    private suspend fun checkAndNotifyReminders() {
        val now = System.currentTimeMillis()
        val preExpiryThreshold = now + TimeUnit.DAYS.toMillis(PRE_EXPIRY_DAYS)

        // Get all expenses with reminders
        val allExpenses = expenseRepository.getAllExpenses().first()
        Log.d(TAG, "Checking ${allExpenses.size} expenses for reminders")

        var notificationCount = 0

        allExpenses.forEach { expense ->
            // Skip if reminder is disabled, dismissed, or pre-expiry notification already sent
            if (!expense.reminderEnabled) {
                Log.d(TAG, "Expense ${expense.id} (${expense.category}): reminder disabled – skip")
                return@forEach
            }

            if (expense.reminderDismissed) {
                Log.d(TAG, "Expense ${expense.id} (${expense.category}): reminder dismissed – skip")
                return@forEach
            }

            if (expense.preExpiryNotificationSent) {
                Log.d(TAG, "Expense ${expense.id} (${expense.category}): notification already sent – skip")
                return@forEach
            }

            // Check if either reminder date or mileage exists
            if (expense.reminderDate == null && expense.reminderMileage == null) {
                return@forEach
            }

            var shouldNotify = false
            var message = ""

            // ── Date-based check ──────────────────────────────────────────
            // Fire if the reminder date is:
            //   • already overdue (reminderDate <= now), OR
            //   • within PRE_EXPIRY_DAYS from now (reminderDate <= preExpiryThreshold)
            expense.reminderDate?.let { reminderDate ->
                if (reminderDate <= preExpiryThreshold) {
                    shouldNotify = true
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(reminderDate))
                    message = if (reminderDate <= now) {
                        // Already overdue
                        applicationContext.getString(
                            R.string.notification_reminder_overdue_message,
                            expense.category,
                            formattedDate
                        )
                    } else {
                        applicationContext.getString(
                            R.string.notification_reminder_date_message,
                            expense.category,
                            formattedDate
                        )
                    }
                    Log.d(TAG, "Date reminder matched for expense ${expense.id}: ${expense.category} (date=$formattedDate, overdue=${reminderDate <= now})")
                }
            }

            // ── Mileage-based check ───────────────────────────────────────
            // Fire if the remaining km is within PRE_EXPIRY_KM, or already exceeded.
            expense.reminderMileage?.let { targetMileage ->
                val car = carRepository.getCarById(expense.carId).first()
                car?.let {
                    val currentMileage = it.currentOdometer.toInt()
                    val remainingKm = targetMileage - currentMileage

                    Log.d(TAG, "Checking mileage for expense ${expense.id}: current=$currentMileage, target=$targetMileage, remaining=$remainingKm")

                    if (remainingKm <= PRE_EXPIRY_KM) {
                        shouldNotify = true
                        val displayKm = remainingKm.coerceAtLeast(0)
                        message = if (message.isEmpty()) {
                            applicationContext.getString(
                                R.string.notification_reminder_mileage_message,
                                expense.category,
                                displayKm
                            )
                        } else {
                            // Combine both date and mileage messages
                            applicationContext.getString(
                                R.string.notification_reminder_both_message,
                                expense.category,
                                displayKm
                            )
                        }
                        Log.d(TAG, "Mileage reminder matched for expense ${expense.id}: ${expense.category}, $remainingKm km remaining")
                    }
                }
            }

            // Send notification if threshold met
            if (shouldNotify) {
                Log.d(TAG, "Sending notification for expense ${expense.id}: ${expense.category}")
                notificationHelper.showPreExpiryNotification(
                    expenseId = expense.id,
                    category = expense.category,
                    message = message
                )

                // Mark notification as sent so we don't re-notify
                expenseRepository.updateExpense(
                    expense.copy(preExpiryNotificationSent = true)
                )
                notificationCount++
            }
        }

        Log.d(TAG, "Reminder check complete – sent $notificationCount notifications")
    }
}
