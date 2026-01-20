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
            Log.d(TAG, "Starting reminder check")

            if (!notificationHelper.areNotificationsEnabled()) {
                Log.d(TAG, "Notifications disabled, skipping check")
                return Result.success()
            }

            checkAndNotifyReminders()
            Log.d(TAG, "Reminder check completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during reminder check", e)
            Result.failure()
        }
    }

    private suspend fun checkAndNotifyReminders() {
        val now = System.currentTimeMillis()
        val oneDayInMillis = TimeUnit.DAYS.toMillis(PRE_EXPIRY_DAYS)
        val preExpiryThreshold = now + oneDayInMillis

        // Get all expenses with reminders
        val allExpenses = expenseRepository.getAllExpenses().first()
        Log.d(TAG, "Checking ${allExpenses.size} expenses")

        var notificationCount = 0

        allExpenses.forEach { expense ->
            // Skip if reminder is disabled, dismissed, or pre-expiry notification already sent
            if (!expense.reminderEnabled) {
                return@forEach
            }

            if (expense.reminderDismissed) {
                return@forEach
            }

            if (expense.preExpiryNotificationSent) {
                return@forEach
            }

            // Check if either reminder date or mileage exists
            if (expense.reminderDate == null && expense.reminderMileage == null) {
                return@forEach
            }

            var shouldNotify = false
            var message = ""

            // Check date-based reminder (1 day before)
            expense.reminderDate?.let { reminderDate ->
                if (reminderDate in (now + 1)..preExpiryThreshold) {
                    shouldNotify = true
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(reminderDate))
                    message = applicationContext.getString(
                        R.string.notification_reminder_date_message,
                        expense.category,
                        formattedDate
                    )
                    Log.d(TAG, "Date reminder matched for expense ${expense.id}: ${expense.category}")
                }
            }

            // Check mileage-based reminder (within 500 km)
            expense.reminderMileage?.let { targetMileage ->
                val car = carRepository.getCarById(expense.carId).first()
                car?.let {
                    val currentMileage = it.currentOdometer.toInt()
                    val remainingKm = targetMileage - currentMileage

                    Log.d(TAG, "Checking mileage for expense ${expense.id}: current=$currentMileage, target=$targetMileage, remaining=$remainingKm")

                    if (remainingKm in 1..PRE_EXPIRY_KM) {
                        shouldNotify = true
                        message = if (message.isEmpty()) {
                            applicationContext.getString(
                                R.string.notification_reminder_mileage_message,
                                expense.category,
                                remainingKm
                            )
                        } else {
                            // Combine both date and mileage messages
                            applicationContext.getString(
                                R.string.notification_reminder_both_message,
                                expense.category,
                                remainingKm
                            )
                        }
                        Log.d(TAG, "Mileage reminder matched for expense ${expense.id}: ${expense.category}, $remainingKm km remaining")
                    }
                }
            }

            // Send notification if threshold met
            if (shouldNotify) {
                Log.d(TAG, "Sending notification for expense ${expense.id}")
                notificationHelper.showPreExpiryNotification(
                    expenseId = expense.id,
                    category = expense.category,
                    message = message
                )

                // Mark notification as sent
                expenseRepository.updateExpense(
                    expense.copy(preExpiryNotificationSent = true)
                )
                notificationCount++
            }
        }

        Log.d(TAG, "Sent $notificationCount notifications")
    }
}
