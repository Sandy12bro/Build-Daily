package com.example.builddaily.util

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class HydrationWatchdogWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        android.util.Log.d("HydrationWatchdog", "Ensuring hydration alarms are active...")
        HydrationScheduler.kickstart(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "hydration_watchdog_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<HydrationWatchdogWorker>(
                1, TimeUnit.HOURS
            ).setConstraints(constraints)
             .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
