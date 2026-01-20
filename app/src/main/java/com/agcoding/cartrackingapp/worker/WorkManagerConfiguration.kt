package com.agcoding.cartrackingapp.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.util.NotificationHelper
import javax.inject.Inject

class ReminderWorkerFactory @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository,
    private val notificationHelper: NotificationHelper
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            ReminderCheckWorker::class.java.name -> {
                ReminderCheckWorker(
                    appContext,
                    workerParameters,
                    expenseRepository,
                    carRepository,
                    notificationHelper
                )
            }
            else -> null
        }
    }
}

class CustomWorkManagerConfiguration @Inject constructor(
    private val workerFactory: ReminderWorkerFactory
) {
    fun getConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
